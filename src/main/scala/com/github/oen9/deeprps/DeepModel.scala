package com.github.oen9.deeprps

import org.datavec.image.recordreader.ImageRecordReader
import java.io.File
import org.datavec.api.split.FileSplit
import org.datavec.image.loader.BaseImageLoader
import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.image.transform.MultiImageTransform
import org.datavec.image.transform.ShowImageTransform
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.learning.config.Adam
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer
import org.nd4j.linalg.activations.Activation
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer
import org.deeplearning4j.nn.conf.layers.PoolingType
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.optimize.listeners.EvaluativeListener
import org.deeplearning4j.optimize.api.InvocationType
import org.datavec.image.loader.NativeImageLoader

object DeepModel {
  val seed = 12345L
  val randNumGen = new java.util.Random(seed)
  val width = 28
  val height = 28
  val nChannels = 3
  val nEpochs = 1

  def loadAndEval(loadModelPath: String, img: File): Unit = {
    val model = MultiLayerNetwork.load(new File(loadModelPath), false)
    println("eval: " + img.toString())

    val imgLoader = new NativeImageLoader(height, width, nChannels)
    val image = imgLoader.asMatrix(img)
    val result = model.feedForward(image, false)
    import scala.jdk.CollectionConverters._
    println("paper \t\t rock \t\t scissors")
    println(result.asScala.last.toFloatMatrix().toVector.map(_.toVector).flatten.mkString("\t"))
  }

  def trainAndSave(savePath: String, trainPath: String): Unit = {
    val traintIter = createDataIter(s"$trainPath/train/")
    val testIter = createDataIter(s"$trainPath/test/")

    println(traintIter.getLabels())
    val labelNum = traintIter.getLabels().size()

    println("build model")
    val conf = new NeuralNetConfiguration.Builder()
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

    val model = new MultiLayerNetwork(conf)
    model.init()

    println("Train model...")
    //Print score every 10 iterations and evaluate on test set every epoch
    model.setListeners(new ScoreIterationListener(10), new EvaluativeListener(testIter, 1, InvocationType.EPOCH_END));
    model.fit(traintIter, nEpochs);

    println("Saving model to tmp folder: " + savePath);
    model.save(new File(savePath), true);

    println("****************Training finished********************");
  }

  private def createDataIter(path: String): RecordReaderDataSetIterator = {
    val parentDir = new File(path)
    val allowedExtensions = BaseImageLoader.ALLOWED_FORMATS
    val filesInDir = new FileSplit(parentDir, allowedExtensions, randNumGen)

    val labelMaker = new ParentPathLabelGenerator()
    //val pathFilter = new BalancedPathFilter(randNumGen, allowedExtensions, labelMaker)
    //filesInDir.sample(pathFilter)

    val recordReader = new ImageRecordReader(height, width, nChannels, labelMaker)
    val transform = new MultiImageTransform(randNumGen, new ShowImageTransform("Display - before "))

    recordReader.initialize(filesInDir, transform)
    val outputNum = recordReader.numLabels()

    //println("outputNum: " + outputNum)

    val batchSize = 10 // 10
    val labelIndex = 1

    val dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, outputNum)
    dataIter
  }
}
