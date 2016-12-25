import linkifyHtml from 'linkifyjs/html';

/**
 * Convert links in a text to clickable HTML anchors
 */
function formatLinks(text) {
  return linkifyHtml(text);
}

export default {
  formatLinks
};
