package com.example.cultino;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private EditText name,email;
    Button submit;
    private ImageView userprofilephoto;
    TextView addphoto;
    private ProgressDialog pd;
    private DatabaseReference reference;
    FirebaseAuth auth;
    private Object CropImage;
    private StorageTask uploadTask;
    private Object CropImageView;
    private Uri mImageUri;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        submit=findViewById(R.id.btn);

        userprofilephoto=findViewById(R.id.userprofilephoto);
        addphoto=findViewById(R.id.addphoto);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        storageReference= FirebaseStorage.getInstance().getReference().child("Uploads");
    /*    FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);

                Picasso.get().load(user.getImageurl()).into(userprofilephoto);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
        userprofilephoto.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {
                com.theartofdev.edmodo.cropper.CropImage.activity().setCropShape(com.theartofdev.edmodo.cropper.CropImageView.CropShape.OVAL).start(MainActivity.this);

            }
        });

        submit.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {
                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Please Wait.....");
                pd.show();
                String str_name = name.getText().toString();
                String str_email = email.getText().toString();
                if (TextUtils.isEmpty(str_name) || TextUtils.isEmpty(str_email) ) {

                    Toast.makeText(MainActivity.this, "All Fields are Required ", Toast.LENGTH_SHORT).show();
                }
                else if (str_name.length() < 6) {
                    Toast.makeText(MainActivity.this, "Enter your full name", Toast.LENGTH_SHORT).show();
                }else {

                    register(str_name,str_email);

                }

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK){
            CropImage.ActivityResult result = com.theartofdev.edmodo.cropper.CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            uploadImage();
        }




        else {
            Toast.makeText(this, "Something gone wrong !", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if (mImageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task <Uri>task) {
                    if (task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        String miUrlOk = downloadUri.toString();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map1 = new HashMap<>();
                        map1.put("imageurl", miUrlOk);
                        reference.updateChildren(map1);
                        pd.dismiss();

                    }else {
                        Toast.makeText(MainActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void register(final String name,final String email) {
        reference= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email,name).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                HashMap<String,Object> map=new HashMap<>();
                map.put("name",name);
                map.put("email",email);
                map.put("imageurl","https://firebasestorage.googleapis.com/v0/b/vsaminternet.appspot.com/o/images%2FAkshay%2F64572.png?alt=media&token=bbde0b9e-781d-4190-b266-8f71fbf01e74");
                map.put("id",auth.getCurrentUser().getUid());
                reference.child("Users").child(auth.getCurrentUser().getUid()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, name+""+email, Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(MainActivity.this, StartActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();



                        }
                    }
                });



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(MainActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}