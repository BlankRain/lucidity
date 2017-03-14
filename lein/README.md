# lein-lucid

metadata and documentation management tool

## Install

Add [![Clojars](https://img.shields.io/clojars/v/im.chit/lein-lucid.svg)](https://clojars.org/im.chit/lein-lucid) to the `:plugins` vector of `project.clj`

## Usage

The following commands can be run: 

     $ lein lucid (watch)  - default, watches project for changes and updates documentation accordingly
     $ lein lucid import   - imports docstrings from test files
     $ lein lucid purge    - purges docstrings from code
     $ lein lucid publish  - generates documentation from project

## License

Copyright © 2017 Chris Zheng

Distributed under the MIT License
