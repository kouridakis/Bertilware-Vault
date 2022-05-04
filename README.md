# Bertilware Vault
A secure and open source password manager that is written in Java and uses the JavaFX library.

## Features
* Encrypts every user's data using AES-256
* Supports syncing data between devices connected to the same network.
* Supports importing/exporting passwords from/to CSV files.
* Includes a password generator.
* Includes searching and sorting functionality to help manage huge numbers of saved passwords.
* Includes various themes!

## Installation
**Windows:** You can use either the setup executable or the jar file found in the releases section.

**Linux:** You can use the same jar file as with Windows. 

**MacOS:** The same jar file should work as well, though it has not been tested.

The setup executable is compiled using Inno Setup and include:
* A jar file, compiled using Apache Maven.
* An executable file, compiled using Launch4j.
* A bundled Java Runtime Environment, compiled by running jlink on an Eclipse Adoptium JDK.
* A license file.
* An icon file. 

## Notes
If you plan on using the syncing feature, make sure you use **_the same username and password on both devices._**

All user files are saved within `/.bertilware/vault/` in your home directory. 