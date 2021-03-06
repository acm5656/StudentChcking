package com.example.checkingsystem.student.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checkingsystem.LoginActivity;
import com.example.checkingsystem.R;
import com.example.checkingsystem.VerifyFaceActivity;
import com.example.checkingsystem.entity.AuthorityInfo;
import com.example.checkingsystem.entity.CourseShow;
import com.example.checkingsystem.entity.ResultObj;
import com.example.checkingsystem.entity.Student;
import com.example.checkingsystem.entity.StudentCourseTimeTable;
import com.example.checkingsystem.student.activity.StudentCheckingActivity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import util.ChangeTypeUtil;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.PathUtil;

public class StudentCheckingFragment extends Fragment implements View.OnClickListener {

    public static final int TRUE = 1;
    public static final int FALSE = 0;
    private OnFragmentInteractionListener mListener;
    private View view;
    private Button verfiButton;
    private Button checkingButton;
    public final static int FACE_VERIFY_RESULT = 1;
    private BluetoothReceiver bluetoothReceiver;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private ArrayList<String> pairedMaclist;
    private boolean doQuery = true;
    private ProgressDialog progressDialog;
    private boolean sendMac = false;
    private String macAddress;
    private int i = 0;
    private String studentID;
    EditText editText ;
    ObjectMapper objectMapper = new ObjectMapper();
    ResultObj<Student> resultObj;
    private String studentFaceID;
    IntentFilter intentFileter;
    private TextView courseNameTextView;
    private TextView couseCodeTextView;
    private String courseID;
    Thread thread;
    Handler handlerStuID = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case TRUE:
                    doQuery = true;
                    sendMac = false;
                    getCheckingAuthority();
                    break;
                case FALSE:
                    Toast.makeText(getContext(), "操作失败请稍后再试", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
    HttpCallbackListener httpCallbackListener = new HttpCallbackListener() {
        @Override
        public void onFinish(String response) {

            try {
                resultObj = objectMapper.readValue(response.getBytes(), new TypeReference<ResultObj<Student>>(){});
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(resultObj.getMeta().getResult())
            {
                Message message = new Message();
                message.what = TRUE;
                studentID = resultObj.getData().getStudentId();
                studentFaceID = resultObj.getData().getStudentFacecode();
                handlerStuID.sendMessage(message);
            }else
            {
                Message message = new Message();
                message.what = FALSE;
                handlerStuID.sendMessage(message);
            }
        }

        @Override
        public void onError(Exception e) {

            Message message = new Message();
            message.what = FALSE;
            handlerStuID.sendMessage(message);
        }
    };
    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            studentID = editText.getText().toString();
            String path = HttpUtil.urlIp+PathUtil.STUDENT_GET_STUDENT_ID;
            String data = "studentNo="+studentID;
            HttpUtil.sendHttpGetRequest(path+"?"+data,httpCallbackListener);

        }
    };
    Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case TRUE:
                    doQuery = false;
                    progressDialog.dismiss();
                    startVerifyFace();
                    break;
                case FALSE:
                    Toast.makeText(getContext(),"未获得考勤权限，请确保周围有已授权设备",Toast.LENGTH_SHORT).show();
                    break;
            }


        }
    };

    HttpCallbackListener httpCallbackListenerGetAuthority = new HttpCallbackListener() {
        @Override
        public void onFinish(String response) {
            ResultObj resultObj = ChangeTypeUtil.getResultObj(response);

            if(resultObj.getMeta().getResult()) {
                Message message = new Message();
                message.what = TRUE;
                handler.sendMessage(message);

            }else {
                sendMac = false;
            }
        }

        @Override
        public void onError(Exception e) {
            sendMac = false;
        }
    };

    class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //得到intent里面的信息
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //System.out.println(device.getAddress());
//                mArrayAdapter.add(device.getName() + "\n" + device.getAddress()+"\n"+intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI));
                if((intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI)+100)>=40)
                {
                    pairedMaclist.add(device.getAddress());
                }

//                list.setAdapter(mArrayAdapter);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_student_checking, container, false);
        verfiButton = (Button)view.findViewById(R.id.fragment_student_checking_button);
        verfiButton.setOnClickListener(this);
        checkingButton = (Button)view.findViewById(R.id.fragment_student_help_student_checking_button);
        checkingButton.setOnClickListener(this);
        courseID = ((StudentCheckingActivity)getActivity()).courseID;
        intentFileter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothReceiver = new BluetoothReceiver();

        courseNameTextView = (TextView)view.findViewById(R.id.fragment_student_checking_name);
        couseCodeTextView = (TextView)view.findViewById(R.id.fragment_student_checking_course_code);
        CourseShow courseShow = StudentCourseIndexFragment.courseShow;
        couseCodeTextView.setText(courseShow.getId());
        courseNameTextView.setText(courseShow.getName());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.fragment_student_checking_button:
                if(LoginActivity.studentStatic.getStudentFacecode()==null||LoginActivity.studentStatic.getStudentFacecode().trim().equals(""))
                {
                    Toast.makeText(getContext(),"没有进行人脸注册，不能进行考勤",Toast.LENGTH_SHORT).show();
                }else {
                    doQuery = true;
                    sendMac = false;
                    getCheckingAuthority();
                    studentID = LoginActivity.studentStatic.getStudentId();
                    studentFaceID = LoginActivity.studentStatic.getStudentFacecode();
                }
                break;
            case R.id.fragment_student_help_student_checking_button:
                showInputDialog();

                break;
        }
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case FACE_VERIFY_RESULT:
                if(resultCode == getActivity().RESULT_OK) {
                    String dataStr = data.getStringExtra("data_return");
                    Toast.makeText(getContext(),dataStr,Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    public void startVerifyFace()
    {
        Intent intent = new Intent(getActivity(), VerifyFaceActivity.class);
        intent.putExtra("studentID",studentID);
        intent.putExtra("mac",macAddress);
        intent.putExtra("studentFaceID",studentFaceID);
        startActivityForResult(intent,FACE_VERIFY_RESULT);
    }
    private void openBluetooth() {
        //bluetoothAdapter.enable();
        if(bluetoothAdapter != null){
                Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);//设置持续时间（最多300秒）
                startActivity(discoveryIntent);

            Class serviceManager = null;
            try {
                serviceManager = Class.forName("android.bluetooth.BluetoothAdapter");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Method method = null;
            try {
                method = serviceManager.getMethod("setDiscoverableTimeout", String.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            try {
                method.invoke(serviceManager.newInstance(), 30);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            }



//            Log.e("test_local",MacUtil.getBtAddressViaReflection());
            String macAddress = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), "bluetooth_address");
            //得到所有已经被对的蓝牙适配器对象
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

            if(devices.size() > 0){
                for(Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext();){
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                    //得到远程蓝牙设备的地址
                    System.out.println(bluetoothDevice.getAddress());
                }
            }

        }
        else {
            //System.out.println("没有蓝牙设备");
            Toast.makeText(getContext(), "没有蓝牙设备",Toast.LENGTH_SHORT).show();
        }
    }

    public void getCheckingAuthority()
    {
        getActivity().registerReceiver(bluetoothReceiver, intentFileter);
        openBluetooth();
        progressDialog = ProgressDialog.show(getActivity(),"考勤获权", "请稍等，确保一米内有已经获权的设备", true, true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                doQuery = false;
            }
        });
        i = 0;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(doQuery) {
                    if(!sendMac) {
                        long beginTime = System.currentTimeMillis();
                        bluetoothAdapter.startDiscovery();
                        pairedMaclist = new ArrayList<String>();
                        long endTime = System.currentTimeMillis();
                        while (endTime - beginTime < 3000) {

                            endTime = System.currentTimeMillis();
                        }
                        i++;
                        bluetoothAdapter.cancelDiscovery();
                        if(Build.VERSION.SDK_INT >= 23) {
                            macAddress = android.provider.Settings.Secure.getString(getActivity().getContentResolver(), "bluetooth_address");

                        }else {
                            macAddress = bluetoothAdapter.getAddress();
                        }
                        String path = HttpUtil.urlIp+ PathUtil.STUDENT_GET_CHECKING_AUTHORITY;
                        AuthorityInfo authorityInfo = new AuthorityInfo();
                        authorityInfo.setMacList(pairedMaclist);
                        authorityInfo.setStuMac(macAddress);
                        authorityInfo.setVirtualCourseId(courseID);

                        String data = ChangeTypeUtil.getJSONString(authorityInfo);
                        HttpUtil.sendHttpPostRequest(path,httpCallbackListenerGetAuthority,data,HttpUtil.CONTENT_TYPE_IS_APPLICATION_JSON);
                        sendMac = true;
                        if(i>20)
                        {
                            doQuery = false;
                            progressDialog.dismiss();
                            Message mesage = new Message();
                            mesage.what = FALSE;
                            handler.sendMessage(mesage);
                        }
                    }
                }
                getActivity().unregisterReceiver(bluetoothReceiver);
            }
        });
        thread.start();
    }
    private void showInputDialog() {
    /*@setView 装入一个EditView
     */
        editText = new EditText(getContext());
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(getContext());
        inputDialog.setTitle("请输入学号").setView(editText);
        inputDialog.setPositiveButton("确定",
                onClickListener).show();
    }



}
