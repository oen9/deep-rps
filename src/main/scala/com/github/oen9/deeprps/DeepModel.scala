package com.github.oen9.deeprps

import zio._
import zio.logging._
import cats.implicits._
import zio.interop.catz.core._

import java.io.File
import org.datavec.image.recordreader.ImageRecordReader
import org.datavec.api.split.FileSplit
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
import java.awt.image.BufferedImage

object DeepModel {
  val seed = 12345L
  val randNumGen = new java.util.Random(seed)
  val width = 28
  val height = 28
  val nChannels = 3
  val nEpochs = 1

  def evalBufferedImg(loadModelPath: String, bImg: BufferedImage) = {
    def loadImage(imgLoader: NativeImageLoader) = ZIO.effect(imgLoader.asMatrix(bImg))
    def parseNetworkResult(nRes: Vector[INDArray]) = for {
      partialResults <- nRes
        .last
        .toFloatMatrix
        .toVector
        .map(_.toVector)
        .flatten
        .zip(RpsType.outcomes)
        .traverse(v => {logInfo(s"${v._1} <- ${v._2}") as v})
      finalResult = partialResults
        .maxBy(_._1)
        ._2
    } yield finalResult

    for {
      _ <- logDebug("eval bufferedImage")
      model <- loadModel(loadModelPath)
      imgLoader <- createImageLoader
      loadedImg <- loadImage(imgLoader)
      networkResult = feedForward(model, loadedImg)
      evalResult <- parseNetworkResult(networkResult)
    } yield evalResult
  }

  def evalFiles(loadModelPath: String, imgs: Vector[File]) = {
    def loadImage(imgLoader: NativeImageLoader, img: File) = ZIO.effect(imgLoader.asMatrix(img))
    def logResults(imgName: String, result: Vector[INDArray]) = for {
      _ <- logInfo(s"eval: $imgName")
      _ <- logInfo("     paper      rock  scissors")
      _ <- logInfo(
        result
          .last
          .toFloatMatrix()
          .toVector
          .map(_.toVector)
          .flatten
          .map(v => f"$v%10.3f")
          .mkString(""))
    } yield ()

    def evalImg(model: MultiLayerNetwork, imgLoader: NativeImageLoader, img: File) = for {
      loadedImg <- loadImage(imgLoader, img)
      result = feedForward(model, loadedImg)
      _ <- logResults(img.getName(), result)
    } yield ()

    for {
      model <- loadModel(loadModelPath)
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
      _ <- logInfo(s"Labels: ${trainIter.getLabels()}")
      labelNum = trainIter.getLabels().size()
      _ <- if (labelNum != 3) logWarn("Warning! labels != 3") else ZIO.unit

      _ <- logInfo("Build model")
      model = buildModel(labelNum)

      _ <- logInfo("Train model...")
      _ <- trainModel(model, trainIter, testIter)

      _ <- logInfo(s"Saving model to $savePath")
      _ <- ZIO.effect(model.save(new File(savePath), true))

      _ <- logInfo("Training finished")
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

  private def loadModel(loadModelPath: String): Task[MultiLayerNetwork] = ZIO.effect(MultiLayerNetwork.load(new File(loadModelPath), false))
  private def createImageLoader: Task[NativeImageLoader] = ZIO.effect(new NativeImageLoader(height, width, nChannels))
  private def feedForward(model: MultiLayerNetwork, img: INDArray): Vector[INDArray] = { // result:    paper <-> rock <-> scissors
    import scala.jdk.CollectionConverters._
    model.feedForward(img).asScala.toVector
  }
}
