<template>
  <el-dialog
    custom-class="answerworkbook-dialog"
    title="문제집 만들기"
    v-model="state.dialogVisible"
    @close="handleClose"
  >
  <el-row :gutters="24">
    <el-col>
      <el-form
        :model="answerbookForm"
        :rules="rules"
        ref="answerbookForm">
        <el-form-item label="제목" prop="title">
          <el-input v-model="state.form.title" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item
          label="파일 업로드"
          prop="uploading"
        >
          <el-upload
            action="https://jsonplaceholder.typicode.com/posts/"
            accept=".pdf"
            :on-change="fileChange"
            :auto-upload="false"
            limit="1"
            ref="pdfUpload"
          >
          <el-button plain type="success">Upload</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button @click="addRow" plain type="success"><i class="el-icon-circle-plus-outline"></i>  Add</el-button>
          <el-table
            style="margin-top: 10px"
            height="400"
            :data="state.tableData">
            <template #empty>
              <h3>"Add" 버튼을 누르고 답안을 작성해주세요!</h3>
            </template>
            <el-table-column
              label="NO."
              type="index">
            </el-table-column>
            <el-table-column prop="answer" label="Answer">
              <template #default="scope">
                  <el-input size="small"
                    placeholder="답을 입력하세요. EX) 1"
                    style="text-align:center"
                    v-model="scope.row.answer" controls-position="right"></el-input>
              </template>
            </el-table-column>
            <el-table-column align='right' width="50" >
              <template #default="scope">
                <el-button icon="el-icon-circle-close" @click="deleteRow(scope.$index, scope.row)" type="text" size="small">
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item align='right'>
          <el-button plain type="success" @click="handleClick" >만들기</el-button>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
  </el-dialog>
</template>
<script>
import { reactive, computed, ref, onMounted } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import VuePdfEmbed from 'vue-pdf-embed';

export default {
  name: "answerworkbook-dialog",

  components: {
      VuePdfEmbed,
    },

  props: {
    open: {
      type: Boolean,
      default: false
    }
  },

  setup(props, { emit }) {
    const store = useStore();
    const answerbookForm = ref(null);
    const pdfUpload = ref(null);
    const pdfRef = ref(null);
    const router = useRouter();

    const state = reactive({
      form: {
        title: "",
        uploading: [],
        align: "left"
      },
      dialogVisible: computed(() => props.open),
      formLabelWidth: "120px",
      page: 1,
      pageCount: 1,
      tableCount: 0,
      tableData: [],
      userpk: computed(() => store.getters["root/getUserpk"]),
    });

    onMounted(() => {
    });

    const handleClose = function() {
      state.tableCount = 0
      state.tableData = []
      state.form.title = ""
      state.form.uploading = []
      emit("closeAnswerWorkbookDialog")
    };

    const fileChange = function(file) {
      const necessary = []
      necessary.push(file['name'])
      necessary.push(file['size'])
      state.form.uploading = necessary
      state.form.uploading = file.raw
    }

    const handleRender = function() {
      state.pageCount = pdfRef.value.pageCount
    }

    const addRow = function() {
      const newRow = {}
      state.tableData = [...state.tableData,newRow]
      ++ state.tableCount
    }

    const deleteRow = function(index,row) {
        state.tableData.splice(index, 1);
        if(state.tableCount > 0)
          -- state.tableCount;
    }

    const handleClick = function() {
      const newtab = []
      for(let val in state.tableData) {
        newtab.push(state.tableData[val]["answer"])
      }

      let body = new FormData()
      body.append("test.userId",state.userpk)
      body.append("test.title",state.form.title)
      body.append("study_thumbnail",state.form.uploading)
      body.append("answer",newtab)
      store.dispatch('root/requestMakeWorkbook',body)
      .then(function(res) {
        pdfUpload.value.submit()
        ElMessage({
          message: "문제집 생성 완료",
          type: "success"
        })
        store.dispatch('root/requestWorkbookList')
        .then(function(res) {
          store.commit('root/setWorkbookList', res.data)
        })
        handleClose()

      })
      .catch(function(err) {
        console.log(err)
      })
    }

    return { answerbookForm, pdfUpload, pdfRef, state, handleClose, handleRender, addRow, deleteRow, fileChange, handleClick };
  }
};
</script>

<style>
.answerworkbook-dialog {
  height: 800px;
  width: 400px;
}
.pdfCol {
  height: 700px;
  border: solid
}
</style>
