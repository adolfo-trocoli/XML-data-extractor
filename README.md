# Data extractor for Open Data Portal of Ayuntamiento de Madrid
> Small Java project aimed to extract data from "Portal de datos abiertos del Ayuntamiento de Madrid" and export it into XML and JSON formats. 

## Table of Contents
* [General Info](#general-information)
* [Functioning](#functioning)
* [Features](#features)
* [Setup](#setup)
	* [Binary installation](#binary-installation)
	* [Compilation from source code](#compilation-from-source-code)
	* [Compilation with test suite](#compilation-with-test-suite)
* [Usage](#usage)
* [Configuration](#configuration)
* [Room for Improvement](#room-for-improvement)
* [Design patterns used](#design-patterns-used)
* [Technologies Used](#technologies-used)
* [Project Status](#project-status)
* [Acknowledgements](#acknowledgements)
* [Contact](#contact)
* [Class diagram](#class-diagram)
* [Screenshots](#screenshots)


## General Information
This program was designed to extract data from the open data portal of Madrid's townhall. It is able to scan the hole XML file, search for the element with the desired code, and get it's information (label, number of related datasets, number of resources associated to each dataset and the name and link of the resources).

## Functioning
The program needs an input file, following the XML Schema of "Portal de datos abiertos del Ayuntamiento de Madrid". You can then query the document searching for a specific code. This arguments must be supplied while calling the program as command-line arguments.

## Features
- Use of SAX Parser conventions from Java.
- XML output.
- JSON output.

## Setup
##### Jar installation
- File parser.jar in bin directory is fully working and ready to be executed with JVM.

##### Compilation from source code
- All code needed for compilation is included in primary directory. All .java files should be compiled (they already are compiled as .class files in bin directory).
- There is a library (gson) which is needed to be added in compilation proccess. It is found in lib/ directory.

## Argument format
The program needs exactly four arguments to provide it's function:
 - Input file: it must be **readable** XML file.
 - Code to search. It must follow the syntax: {3 or 4 numbers}-{between 3 and 8 alphanumeric characters}. Examples could be *327-ABC*, *922-WXYZ*, *1234-JML2JML3*.
 - Output XML file: It must be a **writable** XML file.
 - Output JSON file: It must be a **writable** JSON file. 
#### Sample input and output files
There are some sample files in the repo to showcase functionality
- Sample input: catalogo.xml
- Sample XML output: salida.xml
- Sample JSON output: salida.json

## Room for Improvement

- Program could be generalised to more data formats.
- Program could be more flexible in number and format of arguments.
- A lot more things that are not really necessary to fulfill it's purpose.
#### Design patterns used

- SAX (Simple API for XML) - Abstract design provided by JDK 11.
- Factory - Used to create complex objects securely.

#### Technologies Used
- Java
- JDK 11

#### Project Status
Project is complete. Any additions, comments and requests are welcomed in the Issues page, which will be reviewed periodically.

#### Acknowledgements
- This program was created as part of a project for "Information Proccessing in Telematic Applications" subject.

#### Contact
Created by [@adolfo-trocoli](github.com/adolfo-trocoli)

LinkedIn [profile](https://www.linkedin.com/in/adolfo-trocol%C3%AD-naranjo-a07250224)




