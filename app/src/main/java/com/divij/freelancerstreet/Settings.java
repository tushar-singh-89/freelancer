package com.divij.freelancerstreet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Settings extends AppCompatActivity {
    private EditText mNameField,mPhoneField;
    private Button mBack,mConfirm;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private String userId , name,phone , profileImageUrl;
    private Uri resultUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        String userSex= Objects.requireNonNull(getIntent().getExtras()).getString("userSex");

        mNameField= findViewById(R.id.Name);
        mPhoneField=findViewById(R.id.Phone);
        mProfileImage=findViewById(R.id.profileImage);
        mConfirm=findViewById(R.id.confirm);
        mBack=findViewById(R.id.back);
        mAuth=FirebaseAuth.getInstance();
        userId= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mCustomerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("userSex").child(userId);

        getUserInfo();
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

    }

    private void getUserInfo() {
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    assert map != null;
                    if(map.get("name")!=null){
                        name= (Objects.requireNonNull(map.get("name"))).toString();
                        mNameField.setText(name);
                    }
                    if(map.get("phone")!=null){
                        phone=(Objects.requireNonNull(map.get("phone"))).toString();
                        mPhoneField.setText(phone);
                    }
                    Glide.clear(mProfileImage);
                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl= (Objects.requireNonNull(map.get("profileImageUrl"))).toString();
                        switch (profileImageUrl) {
                            case "default" :
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                break;

                            default:
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                                break;

                        }
                        Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {
        name = mNameField.getText().toString();
        phone=mPhoneField.getText().toString();

        Map<String, Object> userInfo= new HashMap<>();
        userInfo.put("name",name );
        userInfo.put("name",phone );
        mCustomerDatabase.updateChildren(userInfo);
        if(resultUri!=null){
            final StorageReference filepath= FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);
            Bitmap bitmap=null;

            try {
                bitmap= MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert bitmap != null;
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
            byte[]data= baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                                finish();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map<String, Object> newImage = new HashMap<>();
                            newImage.put("profileImageUrl",uri.toString());
                            mCustomerDatabase.updateChildren(newImage);
                            finish();
                        }
                    });



                }
            });
        }
        else{
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode== Activity.RESULT_OK){
            assert data != null;
            resultUri= data.getData();
            mProfileImage.setImageURI(resultUri);

        }
    }
}
