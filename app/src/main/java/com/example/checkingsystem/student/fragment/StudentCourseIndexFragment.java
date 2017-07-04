package com.example.checkingsystem.student.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.checkingsystem.LoginActivity;
import com.example.checkingsystem.R;
import com.example.checkingsystem.adapter.StudentCourseItemAdapter;
import com.example.checkingsystem.adapter.TeacherCourseItemAdapter;
import com.example.checkingsystem.entity.Course;
import com.example.checkingsystem.entity.CourseShow;
import com.example.checkingsystem.entity.VirtualCourse;
import com.example.checkingsystem.net.GetPictureNet;
import com.example.checkingsystem.student.activity.StudentCheckingActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 那年.盛夏 on 2017/6/28.
 */

public class StudentCourseIndexFragment extends Fragment implements AdapterView.OnItemClickListener {
    Lock lock = new ReentrantLock();
    ListView listView;
    StudentCourseItemAdapter courseItemAdapter;
    View view;
    List<CourseShow> courseList;
    int length;
    public static CourseShow courseShow;
    Handler updateUIHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
           updateUI();
        }
    };
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            updateUI();
            for (int i = 0;i<courseList.size();i++)
            {
                GetPictureNet getPictureNet = new GetPictureNet();
                final int finalI = i;
                getPictureNet.getPicture(courseList.get(i).getImgUrl(), new GetPictureNet.HttpPictureCallbackListener() {
                    @Override
                    public void onFinish(InputStream inputStream) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        courseList.get(finalI).setImgBitmap(bitmap);
                        Message message = new Message();
                        updateUIHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_student_course_index,container,false);
        listView = (ListView) view.findViewById(R.id.fragment_student_course_index_list_view);
        initUI();
        return view;
    }
    private void initUI() {
        listView = (ListView) view.findViewById(R.id.fragment_student_course_index_list_view);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (LoginActivity.studentCourseShow==null) {

                }
                length = LoginActivity.studentCourseShow.size();
                while (length<LoginActivity.studentVirtualList.size())
                {
                    length = LoginActivity.studentCourseShow.size();
                }
                courseList = LoginActivity.studentCourseShow;
                Message message = new Message();
                handler.sendMessage(message);
            }
        }).start();
    }
    public void addCourse(VirtualCourse virtualCourse, Bitmap bitmap)
    {
        Toast.makeText(getContext(),"恭喜你，添加成功",Toast.LENGTH_SHORT).show();
        CourseShow courseShow = new CourseShow();
        courseShow.setId(virtualCourse.getVirtualCourseCode());
        courseShow.setDbID(virtualCourse.getVirtualCourseId());
        courseShow.setName(virtualCourse.getVirtualCourseName());
        courseShow.setImgBitmap(bitmap);
        courseList.add(0,courseShow);
        updateUI();
    }

    public void updateUI()
    {
        courseItemAdapter = new StudentCourseItemAdapter(getContext(),courseList,R.layout.student_list_view_course_item);
        listView.setAdapter(courseItemAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        courseShow = courseList.get(position);
        Intent intent = new Intent(getActivity(), StudentCheckingActivity.class);
        startActivity(intent);
    }
}
