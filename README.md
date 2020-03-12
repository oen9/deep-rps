# deep-rps

rock-paper-scissors game with deep learning

[![Build Status](https://travis-ci.org/oen9/deep-rps.svg?branch=master)](https://travis-ci.org/oen9/deep-rps)
[![CircleCI](https://circleci.com/gh/oen9/deep-rps.svg?style=svg)](https://circleci.com/gh/oen9/deep-rps)

## app

![alt text](https://raw.githubusercontent.com/oen9/deep-rps/master/img/screenshot.png "screenshot")

## features

1. train neural network (fed with images)
1. auto save/load network model to/from `~/.deep-rps/model.zip`
1. evaluate images (works with loaded model)
1. play rock-paper-scissors game (works with loaded model)

### game features

1. player vs bot
1. you are the player
1. random choice for bot
1. play without webcam (select hand on static image)
1. play with webcam
1. live network evaluation

## \-\-help

```
deep-rps 0.0.2
Usage: deep-rps [options]

  -x, --x-gui        run game with GUI
  -e, --eval <file>  image to eval (can be used few times to provide multiple files)
  -t, --train <dir>  train network with provided dir with train and test subdirs
  --help             prints this usage text
  --version          prints app version
```

## maybe in future

1. object detection
1. tool to prepare train/test images
