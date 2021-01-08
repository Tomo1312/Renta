package com.example.partyrental.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.partyrental.Login.EnterNumberFragment;
import com.example.partyrental.Login.RegisterFragment;
import com.example.partyrental.Login.VerifyNumberFragment;
import com.example.partyrental.Login.WelcomeFragment;

public class MyViewPagerAdapter extends FragmentPagerAdapter {


    public MyViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public MyViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return WelcomeFragment.getInstance();
            case 1:
                return EnterNumberFragment.getInstance();
            case 2:
                return VerifyNumberFragment.getInstance();
            case 3:
                return RegisterFragment.getInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
