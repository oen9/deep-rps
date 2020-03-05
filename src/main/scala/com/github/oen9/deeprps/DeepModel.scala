package com.github.oen9.deeprps

import zio._
import zio.console._
import cats.implicits._
import zio.interop.catz.core._

import java.io.File
import org.datavec.image.recordreader.ImageRecordReader
import org.datavec.api.split.FileSplit
import org.datavec.image.loader.BaseImageLoader
import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.image.transform.MultiImageTransform
import org.datavec.image.transform.ShowImageTransform
import org.datavec.image.loader.NativeImageLoader
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer
import org.deeplearning4j.nn.conf.layers.PoolingType
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.optimize.listeners.EvaluativeListener
import org.deeplearning4j.optimize.api.InvocationType
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.linalg.api.ndarray.INDArray

object DeepModel {
  val seed = 12345L
  val randNumGen = new java.util.Random(seed)
  val width = 28
  val height = 28
  val nChannels = 3
  val nEpochs = 1

  def evalFiles(loadModelPath: String, imgs: Vector[File]) = {

    import scala.jdk.CollectionConverters._
    def loadModel = ZIO.effect(MultiLayerNetwork.load(new File(loadModelPath), false))
    def createImageLoader = ZIO.effect(new NativeImageLoader(height, width, nChannels))
    def loadImage(imgLoader: BaseImageLoader, img: File) = ZIO.effect(imgLoader.asMatrix(img))
    def feedForward(model: MultiLayerNetwork, img: INDArray) = model.feedForward(img).asScala.toVector
    def logResults(imgName: String, result: Vector[INDArray]) = for {
      _ <- putStrLn(s"eval: $imgName")
      _ <- putStrLn("paper \t\t rock \t\t scissors")
      _ <- putStrLn(result.last.toFloatMatrix().toVector.map(_.toVector).flatten.mkString("\t"))
    } yield ()

    def evalImg(model: MultiLayerNetwork, imgLoader: BaseImageLoader, img: File) = for {
      loadedImg <- loadImage(imgLoader, img)
      result = feedForward(model, loadedImg)
      _ <- logResults(img.getName(), result)
    } yield ()

    for {
      model <- loadModel
      imgLoader <- createImageLoader
      _ <- imgs.traverse(f => evalImg(model, imgLoader, f))
    } yield ()
  }

  def trainAndSave(savePath: String, trainPath: String) = {
    def buildModel(labelNum: Int): MultiLayerNetwork = {
      val conf = createNetworkConf(labelNum)
      val model = new MultiLayerNetwork(conf)
      model.init()
      model
    }

    def trainModel(model: MultiLayerNetwork,
                   trainIter: RecordReaderDataSetIterator,
                   testIter: RecordReaderDataSetIterator) = ZIO.effect {
      model.setListeners(new ScoreIterationListener(10), new EvaluativeListener(testIter, 1, InvocationType.EPOCH_END))
      model.fit(trainIter, nEpochs)
    }

    for {
      trainIter <- createDataIter(s"$trainPath/train/")
      testIter <- createDataIter(s"$trainPath/test/")
      _ <- putStrLn(s"Labels: ${trainIter.getLabels()}")
      labelNum = trainIter.getLabels().size()
      _ <- if (labelNum != 3) putStrLn("Warning! labels != 3") else ZIO.unit

      _ <- putStrLn("Build model")
      model = buildModel(labelNum)

      _ <- putStrLn("Train model...")
      _ <- trainModel(model, trainIter, testIter)

      _ <- putStrLn(s"Saving model to $savePath")
      _ <- ZIO.effect(model.save(new File(savePath), true))

      _ <- putStrLn("Training finished")
    } yield ()
  }

  private def createDataIter(path: String): Task[RecordReaderDataSetIterator] = ZIO.effect {
    val parentDir = new File(path)
    val allowedExtensions = NativeImageLoader.ALLOWED_FORMATS
    val filesInDir = new FileSplit(parentDir, allowedExtensions, randNumGen)

    val labelMaker = new ParentPathLabelGenerator()
    val recordReader = new ImageRecordReader(height, width, nChannels, labelMaker)
    val transform = new MultiImageTransform(randNumGen, new ShowImageTransform("Display - before "))
    recordReader.initialize(filesInDir, transform)

    val outputNum = recordReader.numLabels()
    val batchSize = 10 // 10
    val labelIndex = 1
    new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum)
  }

  private def createNetworkConf(labelNum: Int) = {
    new NeuralNetConfiguration.Builder()
      .seed(seed)
      .l2(0.0005)
      .weightInit(WeightInit.XAVIER)
      .updater(new Adam(1e-3))
      .list()
      .layer(new ConvolutionLayer.Builder(5, 5)
        .nIn(nChannels)
        .stride(1,1)
        .nOut(20)
        .activation(Activation.IDENTITY)
        .build())
      .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
        .kernelSize(2,2)
        .stride(2,2)
        .build())
      .layer(new ConvolutionLayer.Builder(5, 5)
        .stride(1,1)
        .nOut(50)
        .activation(Activation.IDENTITY)
        .build())
      .layer(new SubsamplingLayer.Builder(PoolingType.MAX)
        .kernelSize(2,2)
        .stride(2,2)
        .build())
      .layer(new DenseLayer.Builder().activation(Activation.RELU)
        .nOut(500).build())
      .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
        .nOut(labelNum)
        .activation(Activation.SOFTMAX)
        .build())
      .setInputType(InputType.convolutionalFlat(height, width, nChannels)) 
      .build();
  }
}
