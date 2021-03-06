# Gaia Sky

[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
[![Documentation Status](https://readthedocs.org/projects/gaia-sky/badge/?version=latest)](https://gaia.ari.uni-heidelberg.de/gaiasky/docs/html/latest)
[![Issues](https://img.shields.io/badge/issues-open-bbbb00.svg)](https://gitlab.com/langurmonkey/gaiasky/issues)

[**Gaia Sky**](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky) is a real-time 3D Universe application that
runs on Linux, Windows and macOS. It is developed in the framework of
[ESA](https://www.esa.int/ESA)'s [Gaia mission](https://sci.esa.int/gaia) to chart about 1 billion stars of our Galaxy.

A part of Gaia Sky is described in the paper [Gaia Sky: Navigating the Gaia Catalog](https://dx.doi.org/10.1109/TVCG.2018.2864508).

<img src="header.jpg" alt="Gaia Sky header" style="max-height: 20em;" />

To get the latest up-to-date and most complete information,

*  Visit our [**home page**](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky)
*  Read the [**Documentation**](https://gaia.ari.uni-heidelberg.de/gaiasky/docs/html/latest)
*  Submit a [**bug** or a **feature request**](https://gitlab.com/langurmonkey/gaiasky/issues)
*  Follow development news at [@GaiaSky_Dev](https://twitter.com/GaiaSky_Dev)

This file contains the following sections:

1. [Installation instructions and requirements](#1-installation-instructions-and-requirements)
2. [Pre-built packages](#2-pre-built-packages)
3. [Running from source](#3-running-from-source-repository)
4. [Documentation and help](#4-documentation-and-help)
5. [Copyright and licensing information](#5-copyright-and-licensing-information)
6. [Contact information](#6-contact-information)
7. [Credits and acknowledgements](#7-acknowledgements)
8. [Gaia Sky VR](#8-gaia-sky-vr)

##  1. Installation instructions and requirements

### 1.1. Requirements

| | |
|-|-|
| **Operating system**  | Linux / Windows 7+ / macOS, 64-bit                                             |
| **CPU**               | Intel Core i5 3rd Generation or similar. 4 core or higher recommended          |
| **GPU**               | Support for OpenGL 3.2 (4.x recommended),  1 GB RAM                            |
| **Memory**            | 4+ GB RAM                                                                      |
| **Hard drive**        | 1 GB of free disk space (depending on datasets)                                |

### 2. Pre-built packages

This is the Gaia Sky source repository. We recommend using the [pre-built packages](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads)
for the different Operating Systems in case you want a stable and hassle-free experience. We offer prebuilt packages for Linux, macOS or 
Windows [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).


### 3. Running from source repository

In order to compile and run Gaia Sky from source, you need the following installed in your system:

- `JDK11+`
- `git`

First, clone the [GitLab](https://gitlab.com/langurmonkey/gaiasky) repository:

```console
git clone https://gitlab.com/langurmonkey/gaiasky.git
```

Then, run Gaia Sky (Linux, macOS) with the provided script:

```console
cd gaiasky
./gaiasky
```

On Windows, open PowerShell, make sure your `$JAVA_HOME` environment variable points to a valid JDK11+ installation, and run:

```batchfile
.\gradlew.bat core:run
```

Et voilà ! The bleeding edge Gaia Sky is running in your machine.


### 3.1 CLI arguments

Run `gaiasky -h` or `man gaiasky` to find out about how to launch Gaia Sky and what arguments are accepted.

### 3.2 Getting the data

As of version `2.1.0`, Gaia Sky offers an automated way to download all data packs and catalogs from within the application. When Gaia Sky starts, if no base data or catalogs are found, the downloader window will prompt automatically. Otherwise, you can force the download window at startup with the `-d` argument. Just select the data packs and catalogs that you want to download, press `Download now` and wait for the process to finish.

You can also download the **data packs manually** [here](https://gaia.ari.uni-heidelberg.de/gaiasky/files/autodownload/).


##  4. Documentation and help

The most up-to-date documentation of Gaia Sky is always [hosted at gaia.ari.uni-heidelberg.de/gaiasky/docs/html/latest](https://gaia.ari.uni-heidelberg.de/gaiasky/docs/html/latest](https://gaia.ari.uni-heidelberg.de/gaiasky/docs/html/latest). For older versions and other formats, see [here](https://gaia.ari.uni-heidelberg.de/gaiasky/docs).

We also have a mirror at [gaia-sky.rtfd.org](https://gaia-sky.readthedocs.io).

### 4.1. Documentation submodule

In order to add the documentation submodule to the project, do:

```console
git submodule init
git submodule update
```

The documentation project will be checked out in the `docs/` folder.

##  5. Copyright and licensing information

This software is published and distributed under the MPL 2.0
(Mozilla Public License 2.0). You can find the [full license
text here](/LICENSE.md)
or visiting https://opensource.org/licenses/MPL-2.0.

##  6. Contact information

The main webpage of the project is
**[https://www.zah.uni-heidelberg.de/gaia/outreach/gaiasky](https://www.zah.uni-heidelberg.de/gaia/outreach/gaiasky)**. There you can find
the latest versions and the latest information on Gaia Sky.

##  7. Acknowledgements

The latest acknowledgements are always in the [ACKNOWLEDGEMENTS.md](/ACKNOWLEDGEMENTS.md) file.

##  8. Gaia Sky VR

You can run Gaia Sky in VR with Valve's OpenVR with the `-vr` flag.

```console
gaiasky -vr
```

More information on how to make the VR version work properly in the [VR.md](VR.md) file.

