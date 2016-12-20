<template>

  <dialog class="mdl-dialog">
    <h4 class="mdl-dialog__title">Delete Article</h4>
    <div class="mdl-dialog__content">
      <p>
        Are you sure you want to delete this article ? This cannot be reversed.
      </p>
    </div>
    <div class="mdl-dialog__actions">
      <button type="button" @click="confirm()" class="mdl-button">Yes</button>
      <button type="button" @click="hide()" class="mdl-button close">Cancel</button>
    </div>
  </dialog>

</template>

<script>
  import bus from '../Bus'

  const initialId = -1;

  export default {
    name: 'confirm-dialog',
    data: function () {
      return {
        itemId: initialId
      }
    },
    created: function () {
      const vm = this;
      bus.$on('delete-article', function (itemId) {
        vm.itemId = itemId;

        const dialog = document.querySelector('dialog');
        if (!dialog.showModal) {
          dialogPolyfill.registerDialog(dialog);
        }
        dialog.showModal();
      });
    },
    methods: {
      confirm: function () {
        bus.$emit('delete-article-confirmed', this.itemId);
        this.itemId = initialId;

        const confirmDialog = document.querySelector('dialog');
        confirmDialog.close();
      },
      hide: function () {
        this.itemId = initialId;
        const confirmDialog = document.querySelector('dialog');
        confirmDialog.close()
      }
    }
  };
</script>

<style scoped>
</style>
