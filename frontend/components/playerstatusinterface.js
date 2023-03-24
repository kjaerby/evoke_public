
const extensionID = 'hakahioppgkepcchoibkjanlknbjiofg';

window.seekVideo = function (seconds) {
  window.addEventListener('load', function () {
    chrome.runtime.sendMessage(extensionID, { "seekVideo": seconds });
  });
}
