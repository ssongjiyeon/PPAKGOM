<template>
  <el-dialog
    custom-class="register-dialog"
    title="프로필"
    v-model="state.dialogVisible"
    @close="handleClose"
  >
    <el-form
      :model="state.form"
      :rules="state.rules"
      ref="editForm"
      :label-position="state.form.align"
      :disabled="!state.editMode"
    >
      <el-form-item
        prop="id"
        label="아이디"
        :label-width="state.formLabelWidth"
      >
        <el-input
          v-model="state.form.id"
          autocomplete="off"
          disabled="true"
        ></el-input>
      </el-form-item>
      <el-form-item
        prop="name"
        label="이름"
        :label-width="state.formLabelWidth"
      >
        <el-input v-model="state.form.name" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item
        prop="department"
        label="소속"
        :label-width="state.formLabelWidth"
      >
        <el-input v-model="state.form.department" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item
        prop="position"
        label="직책"
        :label-width="state.formLabelWidth"
      >
        <el-input v-model="state.form.position" autocomplete="off"></el-input>
      </el-form-item>
    </el-form>

    <el-row class="dialog-footer">
      <div v-if="!state.editMode">
        <el-button type="primary" @click="state.editMode = !state.editMode"
          >프로필 수정</el-button
        >
        <el-button class="delete-btn" type="danger" @click="clickDelete"
          >회원 탈퇴</el-button
        >
      </div>

      <div v-else>
        <el-button type="primary" @click="clickUpdate">수정</el-button>
        <el-button type="warning" @click="state.editMode = !state.editMode"
          >취소</el-button
        >
      </div>
    </el-row>
  </el-dialog>
</template>
<style>
section.el-container {
  justify-content: center;
}
.register-dialog {
  width: 600px !important;
  height: 400px;
}
.register-dialog .el-dialog__headerbtn {
  float: right;
}
.register-dialog .el-form-item__content {
  margin-left: 0 !important;
  float: right;
  width: 200px;
  display: inline-block;
}
.register-dialog .el-form-item {
  margin-bottom: 20px;
}
.register-dialog .el-form-item__error {
  font-size: 12px;
  color: red;
}
.register-dialog .el-input__suffix {
  display: none;
}
.register-dialog .el-dialog__footer {
  margin: 0 calc(50% - 80px);
  padding-top: 0;
  display: inline-block;
}
.register-dialog .dialog-footer .el-button {
  width: 120px;
  align-self: center;
}
</style>
<script>
import { reactive, computed, ref, onMounted } from "vue";
import { useStore } from "vuex";
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router";

export default {
  name: "Mypage",

  props: {
    open: {
      type: Boolean,
      default: false
    }
  },

  setup(props, { emit }) {
    const store = useStore();
    const router = useRouter();
    const editForm = ref(null);
    /*
      // Element UI Validator
      // rules의 객체 키 값과 form의 객체 키 값이 같아야 매칭되어 적용됨
      //
    */
    const state = reactive({
      form: {
        name: "",
        department: "",
        position: "",
        id: "",
        align: "left"
      },
      rules: {
        name: [{ required: true, validator: validateName, trigger: "blur" }],
        department: [{ validator: validateDepartment, trigger: "blur" }],
        position: [{ validator: validatePosition, trigger: "blur" }]
      },
      dialogVisible: computed(() => props.open),
      editMode: false,
      formLabelWidth: "130px"
    });

    const validateName = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("Please input the Name"));
      } else if (value.length > 30) {
        callback(new Error("You can enter up to 30 characters"));
      } else {
        callback();
      }
    };

    const validateDepartment = (rule, value, callback) => {
      if (value.length > 30) {
        callback(new Error("You can enter up to 30 characters"));
      } else {
        callback();
      }
    };

    const validatePosition = (rule, value, callback) => {
      if (value.length > 30) {
        callback(new Error("You can enter up to 30 characters"));
      } else {
        callback();
      }
    };

    const getUserInfo = () => {
      store
        .dispatch("root/requestReadMyInfo")
        .then(function(result) {
          state.form.id = result.data.userId;
          state.form.name = result.data.name;
          state.form.department = result.data.department;
          state.form.position = result.data.position;
        })
        .catch(function(err) {
          ElMessage.error(err);
        });
    };

    const clickUpdate = function() {
      editForm.value.validate(valid => {
        if (valid) {
          store
            .dispatch("root/requestUpdateMyInfo", {
              name: state.form.name,
              department: state.form.department,
              position: state.form.position
            })
            .then(function(result) {
              ElMessage({
                message: "수정이 완료되었습니다.",
                type: "success"
              });
              state.editMode = !state.editMode;
            })
            .catch(function(err) {
              console.log(err);
            });
        } else {
          ElMessage.error("Validate error!");
        }
      });
    };

    const clickDelete = function() {
      store
        .dispatch("root/requestDeleteMyInfo")
        .then(function(result) {
          store.dispatch("root/requestLogout"); // 로그아웃
          store.commit("root/deleteToken");
          router.push({
            name: "home"
          });

          ElMessage({
            message: "회원탈퇴가 정상적으로 완료되었습니다.",
            type: "success"
          });
          handleClose();
        })
        .catch(function(err) {
          console.log(err);
        });
    };

    onMounted(() => {
      // console.log(loginForm.value)
      getUserInfo();
    });

    const handleClose = function() {
      //state.form.department = "";
      //state.form.position = "";
      //state.form.name = "";
      //state.form.id = "";
      emit("closeMypageDialog");
    };

    return { editForm, state, clickUpdate, handleClose, clickDelete };
  }
};
</script>
