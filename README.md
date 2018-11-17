# RoVer

RoVer is a design interface for authoring human-robot interactions, and providing feedback to designers on social norms that interaction designs violate.

## Getting Started

Currently, RoVer only works with the Prism Model Checker version 4.3.1. You must download the source code from https://www.prismmodelchecker.org.

### Prerequisites

RoVer works on OSX and Linux. 

### Installing

Download and place the Prism source code in the root directory of RoVer. To install Prism, run the following from the folder containing Prism:

```
make clean
make
make binary
```

This generates a prism.jar file, which RoVer uses.

Ensure that you also have the java jdk version 1.8.0_181 installed as well. To run RoVer, execute ./RoVer.sh from the RoVer folder.

If you have any problems or questions about installation, email dporfirio@wisc.edu.

## License

This project is licensed under the BSD 3-Clause License - see the [LICENSE.md](LICENSE.md) file for details

