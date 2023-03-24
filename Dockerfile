FROM ubuntu:20.04

RUN DEBIAN_FRONTEND="noninteractive" apt-get update && apt-get upgrade -y && apt-get dist-upgrade -y 
RUN DEBIAN_FRONTEND="noninteractive" apt-get install -y ffmpeg maven python3 python3-pip curl git wget chromium-browser

RUN pip3 install youtube-dl
RUN pip3 install pycryptodomex
RUN pip3 install requests

RUN wget https://mega.nz/linux/MEGAsync/xUbuntu_20.04/amd64/megacmd-xUbuntu_20.04_amd64.deb
RUN apt install -y ./megacmd-xUbuntu_20.04_amd64.deb 
RUN rm megacmd-*.deb

RUN apt-get install -f

RUN mkdir -p /opt/evoke/misc
COPY target/* /opt/evoke/
COPY config.properties /opt/evoke/ 

WORKDIR /opt/evoke/misc/
RUN git clone https://github.com/phanirithvij/twist.moe.git

WORKDIR /opt/evoke/
CMD ["/usr/bin/java", "-jar", "evoke-1.0-SNAPSHOT.jar"]
