# Thumty
[![CircleCI](https://circleci.com/gh/thumty/thumty.svg?style=svg)](https://circleci.com/gh/thumty/thumty)
[![codecov](https://codecov.io/gh/thumty/thumty/branch/master/graph/badge.svg)](https://codecov.io/gh/thumty/thumty)
[![Dependency Status](https://www.versioneye.com/user/projects/58d95960d6c98d004174757c/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58d95960d6c98d004174757c)

## About
Thumty is on-demand smart resizing and post-processing imaging service, written entirely in Java. 

Thumty is based on [Vertx](http://vertx.io) framework, it's fast has low memory usage,  optimized for serving lots of concurrent connections and processing large images. 

Thumty supports following source image formats:
- JPG
- PNG
- BMP
- GIF (animation is not supported) 
- PSD
- TIFF

## Usage

Once configured and running, transformed image can be simply accessed by URL. All image processing parameters are passed as parts of URL path e.g.

http://example.com/200x200/original/image.jpg - will return "origina/image.jpg" resized to 200x200.
http://example.com/fit-in/-200x200/http://example.com/original/image.jpg - will return mirrored and resized to fit in 200x200 original image located at http://example.com/original/image.jpg .

## Running

### Create minimum configuration file
```json
{
  "loaders": {
    "http" : {}
  }
}
```

### Run Thumty
```bash 
java -jar thumty-server-1.0.1-linux-x86_64.jar -conf config.json
```

Open http://localhost:8080/200x200/placehold.it/350x150
