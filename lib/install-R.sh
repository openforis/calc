#!/bin/sh
add-apt-repository http://cran.rstudio.com/bin/linux/ubuntu
gpg --keyserver pgp.mit.edu --recv-key 51716619E084DAB9
gpg -a --export E084DAB9 | apt-key add -
apt-get update
# Distro specific
apt-get install r-base=3.0.1-3precise
#R  --vanilla --slave -e "install.packages('rJava', repos='http://cran.rstudio.com/')"
R CMD INSTALL rJava_0.9-5.tar.gz

