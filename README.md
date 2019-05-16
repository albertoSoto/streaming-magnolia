# Magnolia Streaming - Retos digitales 2019  

Magnolia low level project integrating Spring MVC 5 as a filter chain into the Magnolia context.

Allows streaming DAM Assets with header-range control, splitting files in 1Mb chunks, generating a professional environment for video streaming.

Uncomment Web flux dependency in magnolia module pom to activate Spring WebFlux with MVC.

Based on Magnolia 6.0

![Magnolia-streaming-module](readme/magnolia-streaming.jpg)

## Features
> Magnolia has grown so much that now is really hard achieving new features. The product is mature enough to solve most of any companies problems. Within the DAM Module you can serve multimedia files including S3 storage from AWS through a plugin. 

> *Problem*: When serving the file as a video it will load all the file which is a HUGE problem on rendering context. 
The Magnolia Streaming module allows you to integrate big videos without 

- Adds Spring MVC 5 support in the Magnolia filter chain in a professional way
- Registers RegionResourceMessageConverter to change behaviour for big files
- Provides basic video components for video streaming
- Improves video performance for partial file streaming


## Usage
> Just compile the project and execute it as a magnolia bundle. 

- Download the full project with maven and java 1.8 already installed in your computer
- Deploy the generated war in your server
- Works as a bundle. Copy the module into your project directly if you need it. 

## Components provided

> A controller with demo files is included 
- Although the main concept of the project is not providing webcomponents there are base components as an example to work with it.

##Behaviour

> Download profile of conventional streaming from DAMModule

You can see how the file is downloaded at once, and it doesn't allow partial content rendering
Content rendering is trough a blocking response and it does not allow changing easy the cursor or video timing

![Original-streaming](readme/streaming-original.jpg)

> Download profile of conventional streaming from DAMModule

Here you are the final behaviour. Time response is much faster and the browser comunicates with the server generating small requests of 1M.
The browser is not block, and changing from one time to another is much faster. 

![Performance-streaming](readme/streaming-performance.jpg)


## Information on Magnolia CMS
This directory is an extended version of a Magnolia 'blossom module' delivered as a full project.
https://docs.magnolia-cms.com
You can use Blossom archetype from Magnolia International to start creating your own template!

## License

MIT

## Contributors

- Magnolia CMS, https://magnolia-cms.com
- Alberto Soto Fernandez, https://github.com/albertoSoto/