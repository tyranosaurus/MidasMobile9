package com.test.midasmobile9.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.test.midasmobile9.R;
import com.test.midasmobile9.util.Encryption;
import com.test.midasmobile9.util.Permission;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.test.midasmobile9.model.JoinModel.*;
import static com.test.midasmobile9.util.ImageUtil.scaleImageDownToFile;


public class JoinActivity extends AppCompatActivity {

    private String mTAG = "JoinActivity";
    private Context mContext = JoinActivity.this;

    @BindView(R.id.linearLayoutJoinActivity)
    LinearLayout linearLayoutJoinActivity;

    /**
     * 회원가입 뷰
     */
    @BindView(R.id.linearLayoutEmailPassword)
    LinearLayout linearLayoutEmailPassword;
    @BindView(R.id.editTextInputEmail)
    EditText editTextInputEmail;
    @BindView(R.id.checkBoxAdmin)
    CheckBox checkBoxAdmin;
    @BindView(R.id.editTextInputAdminCode)
    EditText editTextInputAdminCode;
    @BindView(R.id.editTextInputPasswordFirst)
    EditText editTextInputPasswordFirst;
    @BindView(R.id.editTextInputPasswordSecond)
    EditText editTextInputPasswordSecond;
    @BindView(R.id.textViewDoJoin)
    TextView textViewDoJoin;

    /**
     * 프로필 뷰
     */
    @BindView(R.id.linearLayoutProfileInfo)
    LinearLayout linearLayoutProfileInfo;
    @BindView(R.id.frameLayoutProfileImage)
    FrameLayout frameLayoutProfileImage;
    @BindView(R.id.circleImageViewProfileImage)
    CircleImageView circleImageViewProfileImage;
    @BindView(R.id.editTextNickname)
    EditText editTextNickname;
    @BindView(R.id.editTextPhone)
    EditText editTextPhone;
    @BindView(R.id.editTextPart)
    EditText editTextPart;
    @BindView(R.id.textViewSaveProfile)
    TextView textViewSaveProfile;

    private String strEmail;
    private String strPassWord;
    private String strNickName;
    private String strRootCode;
    private String strPhone;
    private String strPart;
    private int intRoot = 0;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    private final int PICK_FROM_ALBUM = 1;
    private Uri mImageCaptureUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.bind(this);
        editTextPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }


    /**
     * 가입하기-프로필저장 버튼 클릭 이벤트
     */
    @OnClick({R.id.textViewDoJoin, R.id.textViewSaveProfile})
    public void onClickJoinButtonJoin(View view) {
        switch (view.getId()) {
            //가입하기
            case R.id.textViewDoJoin:
                strEmail = editTextInputEmail.getText().toString().trim();
                strPassWord = editTextInputPasswordFirst.getText().toString().trim();
                strRootCode = editTextInputAdminCode.getText().toString().trim();
                if (!emailCheck(strEmail)) {
                    return;
                }
                if (!passwordCheck(strPassWord)) {
                    return;
                }
                if(intRoot==1&&!rootcodeCheck(strRootCode)){
                    return;
                }
                //이메일체크 TASK 실행
                new EmailCheckTask().execute();
                break;
            //프로필저장
            case R.id.textViewSaveProfile:
                strNickName = editTextNickname.getText().toString().trim();
                strPhone = editTextPhone.getText().toString().trim();
                strPart = editTextPart.getText().toString().trim();
                if (!nicknameCheck(strNickName)) {
                    return;
                }
                if(!phoneCheck(strPhone)){
                    return;
                }
                if(!partCheck(strPart)){
                    return;
                }
                new NicknameCheckTask().execute();
                break;
        }
    }

    /**
     * 관리자 체크 박스 체인지 리스너
     * @param button
     * @param checked
     */
    @OnCheckedChanged({R.id.checkBoxAdmin})
    public void onCheckedChangeGroup(CompoundButton button, boolean checked){
        if(checked){
            intRoot = 1;
            editTextInputAdminCode.setVisibility(View.VISIBLE);
        }else{
            intRoot = 0;
            editTextInputAdminCode.setText("");
            editTextInputAdminCode.setVisibility(View.GONE);
        }
    }

    /**
     * 이메일 체크
     * @param email 이메일문자열
     * @return true : 패스 , false : 실패
     */
    private boolean emailCheck(String email) {
        if (email.length() == 0) {
            //이메일 길이가 0일 경우
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.email_length_zero), Snackbar.LENGTH_SHORT).show();
            editTextInputEmail.requestFocus();
            return false;
        }
        String emailRegExp = "[\\w\\~\\-\\.]+@[\\w\\~\\-]+(\\.[\\w\\~\\-]+)+";
        Matcher matcher = Pattern.compile(emailRegExp).matcher(email);
        if (!matcher.matches()) {
            //이메일 형식이 아닐 경우
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.email_combination), Snackbar.LENGTH_SHORT).show();
            editTextInputEmail.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * 비밀번호 체크
     * @param password 비밀번호 문자열
     * @return true : 패스 , false : 실패
     */
    private boolean passwordCheck(String password) {
        if (password.length() == 0) {
            //비밀번호 길이가 0일 경우
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.password_length_zero), Snackbar.LENGTH_SHORT).show();
            editTextInputPasswordFirst.requestFocus();
            return false;
        }
        String pwPattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z]).{9,12}$";
        Matcher matcher = Pattern.compile(pwPattern).matcher(password);
        pwPattern = "(.)\\1\\1\\1";
        Matcher matcher2 = Pattern.compile(pwPattern).matcher(password);
        if (!matcher.matches()) {
            //영문 숫자 특수문자 구분
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.password_combination), Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (matcher2.find()) {
            //같은 문자 4자리 이상
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.password_repeat), Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (password.contains(strEmail)) {
            //아이디 포함 여부
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.password_contain_id), Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (password.contains(" ")) {
            //공백문자 사용 불가
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.password_blank), Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(editTextInputPasswordSecond.getText().toString())) {
            //비밀번호 확인
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.password_not_matched), Snackbar.LENGTH_SHORT).show();
            editTextInputPasswordSecond.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * 관리자 번호 체크
     * @param rootcode 관리자번호 문자열
     * @return true : 패스 , false : 실패
     */
    private boolean rootcodeCheck(String rootcode) {
        if (rootcode.length() == 0) {
            //이메일 길이가 0일 경우
            Snackbar.make(linearLayoutJoinActivity, getString(R.string.rootcode_length_zero), Snackbar.LENGTH_SHORT).show();
            editTextInputAdminCode.requestFocus();
            return false;
        }
        return true;
    }
    /**
     * 닉네임 체크
     * @param nickname 닉네임 문자열
     * @return true : 패스 , false : 실패
     */
    private boolean nicknameCheck(String nickname) {
        if (nickname.length() == 0) {
            //닉네임 길이가 0일 경우
            Snackbar.make(linearLayoutProfileInfo, getString(R.string.nickname_length_zero), Snackbar.LENGTH_SHORT).show();
            editTextNickname.requestFocus();
            return false;
        }
        return true;
    }
    /**
     * 핸드폰번호 체크
     * @param phone 핸드폰번호 문자열
     * @return true : 패스 , false : 실패
     */
    private boolean phoneCheck(String phone) {
        if (phone.length() == 0) {
            //닉네임 길이가 0일 경우
            Snackbar.make(linearLayoutProfileInfo, getString(R.string.phone_length_zero), Snackbar.LENGTH_SHORT).show();
            editTextPhone.requestFocus();
            return false;
        }
        return true;
    }
    /**
     * 부서 체크
     * @param part 부서 문자열
     * @return true : 패스 , false : 실패
     */
    private boolean partCheck(String part) {
        if (part.length() == 0) {
            //닉네임 길이가 0일 경우
            Snackbar.make(linearLayoutProfileInfo, getString(R.string.part_length_zero), Snackbar.LENGTH_SHORT).show();
            editTextPhone.requestFocus();
            return false;
        }
        return true;
    }
    /**
     * 프로필 사진 클릭 이벤트
     */
    @OnClick({R.id.frameLayoutProfileImage})
    public void onClickJoinCircleImageViewProfileImage(View view) {
        TedPermission.with(mContext)
                .setPermissionListener(permissionListener)
                .setDeniedMessage(getString(R.string.permission_denied))
                .setPermissions(permissions)
                .check();
    }
    /**
     * 퍼미션 요청 결과
     */
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Snackbar.make(linearLayoutProfileInfo, getString(R.string.permission_snackbar_text), Snackbar.LENGTH_LONG).setAction("설정", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TedPermission.with(mContext)
                            .setPermissionListener(permissionListener)
                            .setDeniedMessage(getString(R.string.permission_denied))
                            .setPermissions(permissions)
                            .check();
                }
            });
        }
    };

    /**
     * 앨범에서 이미지를 가져온 후 이미지 세팅.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                mImageCaptureUri = data.getData();
                Glide.with(mContext)
                        .load(mImageCaptureUri)
                        .into(circleImageViewProfileImage);
                break;
            }
        }
    }

    /**
     * 가입하기 with Server
     */
    public class JoinTask extends AsyncTask<Void, Void, Map<String, Object>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Map<String, Object> doInBackground(Void... voids) {
            File file = null;
            if (mImageCaptureUri != null)
                file = scaleImageDownToFile(mContext, mImageCaptureUri);
            Map<String, Object> map = getJoinResult(strEmail, strPassWord, strNickName, file, intRoot, strPhone, strPart);
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, Object> map) {
            super.onPostExecute(map);
            if (map == null) {
                // 통신실패
                String message = "인터넷 연결이 원활하지 않습니다. 잠시후 다시 시도해주세요.";
                Snackbar.make(linearLayoutProfileInfo, message, Snackbar.LENGTH_SHORT).show();
            } else {
                boolean result = false;
                String message = null;
                if (map.containsKey("result")) {
                    result = (boolean) map.get("result");
                }
                if (map.containsKey("message")) {
                    message = (String) map.get("message");
                }
                if (result) {
                    //성공
                    Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    //실패
                    Snackbar.make(linearLayoutProfileInfo, message, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 이메일 체크 with Server
     */
    public class EmailCheckTask extends AsyncTask<Void, Void, Map<String, Object>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Map<String, Object> doInBackground(Void... voids) {
            Map<String, Object> map = getEmailCheckResult(strEmail);
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, Object> map) {
            super.onPostExecute(map);
            if (map == null) {
                // 통신실패
                String message = "인터넷 연결이 원활하지 않습니다. 잠시후 다시 시도해주세요.";
                Snackbar.make(linearLayoutEmailPassword, message, Snackbar.LENGTH_SHORT).show();
            } else {
                boolean result = false;
                String message = null;
                if (map.containsKey("result")) {
                    result = (boolean) map.get("result");
                }
                if (map.containsKey("message")) {
                    message = (String) map.get("message");
                }
                if (result) {
                    //성공
                    if(intRoot==1){
                        new RootCodeTask().execute();
                    }else{
                        strPassWord = Encryption.getMD5(strPassWord);
                        textViewDoJoin.setVisibility(View.GONE);
                        linearLayoutEmailPassword.setVisibility(View.GONE);
                        textViewSaveProfile.setVisibility(View.VISIBLE);
                        linearLayoutProfileInfo.setVisibility(View.VISIBLE);
                    }
                } else {
                    //실패
                    Snackbar.make(linearLayoutEmailPassword, message, Snackbar.LENGTH_SHORT).show();
                }
            }

        }
    }
    /**
     * 관리자코드 체크 with Server
     */
    public class RootCodeTask extends AsyncTask<Void, Void, Map<String, Object>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Map<String, Object> doInBackground(Void... voids) {
            Map<String, Object> map = getRootCodeCheckResult(strRootCode);
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, Object> map) {
            super.onPostExecute(map);
            if (map == null) {
                // 통신실패
                String message = "인터넷 연결이 원활하지 않습니다. 잠시후 다시 시도해주세요.";
                Snackbar.make(linearLayoutEmailPassword, message, Snackbar.LENGTH_SHORT).show();
            } else {
                boolean result = false;
                String message = null;
                if (map.containsKey("result")) {
                    result = (boolean) map.get("result");
                }
                if (map.containsKey("message")) {
                    message = (String) map.get("message");
                }
                if (result) {
                    //성공
                    strPassWord = Encryption.getMD5(strPassWord);
                    textViewDoJoin.setVisibility(View.GONE);
                    linearLayoutEmailPassword.setVisibility(View.GONE);
                    textViewSaveProfile.setVisibility(View.VISIBLE);
                    linearLayoutProfileInfo.setVisibility(View.VISIBLE);
                } else {
                    //실패
                    Snackbar.make(linearLayoutEmailPassword, message, Snackbar.LENGTH_SHORT).show();
                }
            }

        }
    }
    /**
     * 닉네임 체크 with Server
     */
    public class NicknameCheckTask extends AsyncTask<Void, Void, Map<String, Object>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Map<String, Object> doInBackground(Void... voids) {
            Map<String, Object> map = getNicknameCheckResult(strNickName);
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, Object> map) {
            super.onPostExecute(map);
            if (map == null) {
                // 통신실패
                String message = "인터넷 연결이 원활하지 않습니다. 잠시후 다시 시도해주세요.";
                Snackbar.make(linearLayoutProfileInfo, message, Snackbar.LENGTH_SHORT).show();
            } else {
                boolean result = false;
                String message = null;
                if (map.containsKey("result")) {
                    result = (boolean) map.get("result");
                }
                if (map.containsKey("message")) {
                    message = (String) map.get("message");
                }
                if (result) {
                    //성공
                    new JoinTask().execute();
                } else {
                    //실패
                    Snackbar.make(linearLayoutProfileInfo, message, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

}
