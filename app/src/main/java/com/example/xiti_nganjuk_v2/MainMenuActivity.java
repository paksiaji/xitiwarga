package com.example.xiti_nganjuk_v2;

import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.xiti_nganjuk_v2.fragments.AddFragment;
import com.example.xiti_nganjuk_v2.fragments.HomeFragment;
import com.example.xiti_nganjuk_v2.fragments.ImagePostFragment;
import com.example.xiti_nganjuk_v2.fragments.ProfileFragment;
import com.example.xiti_nganjuk_v2.fragments.VideoPostFragment;

public class MainMenuActivity extends AppCompatActivity implements HomeFragment.OnFragmentInteractionListener ,
        AddFragment.OnFragmentInteractionListener , ProfileFragment.OnFragmentInteractionListener ,
        ImagePostFragment.OnFragmentInteractionListener, VideoPostFragment.OnFragmentInteractionListener {
    BottomNavigationView bn_main;
    LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        bn_main = findViewById(R.id.bn_main);
        Fragment initialFragment = new HomeFragment();
        LoadFragment(initialFragment,R.id.home_menu);
        bn_main.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                switch (menuItem.getItemId()){
                    case R.id.home_menu :
                        fragment = new HomeFragment();
                        break;

                    case R.id.add_menu :
                        fragment = new AddFragment();
                        break;
                    case R.id.profile_menu :
                        fragment = new ProfileFragment();
                        break;
                }

                return LoadFragment(fragment,menuItem.getItemId());
            }
        });
    }

    private boolean LoadFragment(Fragment fragment,int id){
        if(fragment != null){
            Fragment f = getSupportFragmentManager().findFragmentByTag(String.valueOf(id));
            if(f != null){
                Log.d("MainMenuActivity","Already Added");
                getSupportFragmentManager().beginTransaction().addToBackStack(id+"stack_item").replace(R.id.flContainer,f,String.valueOf(id)).commit();
            }else{
                getSupportFragmentManager().beginTransaction().addToBackStack(id+"stack_item").replace(R.id.flContainer,fragment,String.valueOf(id)).commit();
            }
            return true;
        }

        return false;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.mnEditProfile :
                Toast.makeText(MainMenuActivity.this,"Edit profile Clicked",Toast.LENGTH_SHORT);
                break;
            case R.id.mnLogout :
                Toast.makeText(MainMenuActivity.this, "Logout Clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
