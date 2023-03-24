'use strict'

const $ = require("jquery");
const VideoStream = require('videostream')
const toStream = require('it-to-stream')

$.getScript("https://cdn.jsdelivr.net/npm/ipfs/dist/index.min.js", function (data, textStatus, jqxhr) {
  init();
});

async function init() {
  console.log('IPFS: Initialising')

  if (!window.ipfs) {
    window.ipfs = await Ipfs.create({ repo: 'ipfs-evoke' })
  }

  console.log('IPFS: Ready')
}

window.loadVideo = function (elementId, cid, fallback) {
  console.log("IPFS: loading " + cid + " into " + elementId);

  if ('MediaSource' in window) {

    var waitForIPFS = function (callback) {
      if (window.ipfs) {
        callback();
      } else {
        setTimeout(function () {
          waitForIPFS(callback);
        }, 100);
      }
    };

    waitForIPFS(function () {
      var waitForEl = function (selector, callback) {
        if ($(selector).length) {
          callback();
        } else {
          setTimeout(function () {
            waitForEl(selector, callback);
          }, 100);
        }
      };

      waitForEl("#" + elementId, function () {
        // Set up event listeners on the <video> element from index.html
        const videoElement = document.getElementById(elementId);
        let stream;

        console.log("IPFS: Playing " + cid)

        // Set up the video stream an attach it to our <video> element
        const videoStream = new VideoStream({
          createReadStream: function createReadStream(opts) {
            const start = opts.start

            // The videostream library does not always pass an end byte but when
            // it does, it wants bytes between start & end inclusive.
            // catReadableStream returns the bytes exclusive so increment the end
            // byte if it's been requested
            const end = opts.end ? start + opts.end + 1 : undefined

            console.debug("Stream: Asked for data starting at byte " + start + " and ending at byte " + end)

            // If we've streamed before, clean up the existing stream
            if (stream && stream.destroy) {
              stream.destroy()
            }

            // This stream will contain the requested bytes
            stream = toStream.readable(window.ipfs.cat(cid.trim(), {
              offset: start,
              length: end && end - start
            }))

            // Log error messages
            stream.on('error', (error) => log(error))
            return stream
          }
        }, videoElement)

        videoElement.addEventListener('error', () => console.log(videoStream.detailedError))
      });
    });
  } else {
    const videoElement = document.getElementById(elementId);
    videoElement.setAttribute("src", fallback);
  }
};