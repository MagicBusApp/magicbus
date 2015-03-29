package com.magicbusapp.magicbus;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;


public class MBWizardActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
    MBWizardPagerAdapter mMbWizardPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    ViewPager mViewPager;

    final static String FRASE_1 = "gps";
    final static String FRASE_2 =  "community";
    final static String FRASE_3 = "wiki";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_mbwizard);

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        //
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mMbWizardPagerAdapter = new MBWizardPagerAdapter(getSupportFragmentManager());

        // Set up action bar.
        setUpActionBar();

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mMbWizardPagerAdapter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpActionBar() {
        //getSupportActionBar().hide(;
        //getActionBar().hide();
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class MBWizardPagerAdapter extends FragmentStatePagerAdapter {

        public MBWizardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Bundle args = new Bundle();

            switch(i){
                case 0:
                    args.putString(MBWizardFragment.ARG_OBJECT, FRASE_1);
                    break;
                case 1:
                    args.putString(MBWizardFragment.ARG_OBJECT, FRASE_2);
                    break;
                case 2:
                    args.putString(MBWizardFragment.ARG_OBJECT, FRASE_3);
                    break;
            }
            Fragment fragment = new MBWizardFragment();

            //args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return "#GPS";
                case 1:
                    return "#Community";
                case 2:
                    return "#WikiBus";
            }
            return "";
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class MBWizardFragment extends Fragment {

        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_wizard, container, false);
            Bundle args = getArguments();

            Button buttonFragment = (Button) rootView.findViewById(R.id.buttonFragment);
            FrameLayout frameLayout = (FrameLayout) rootView.findViewById(R.id.wizard_layout);

            if(args.getString(ARG_OBJECT).equalsIgnoreCase(FRASE_1)){
                buttonFragment.setVisibility(View.GONE);
                frameLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_wizard_gps));
            }
            else if(args.getString(ARG_OBJECT).equalsIgnoreCase(FRASE_2)){
                buttonFragment.setVisibility(View.GONE);
                frameLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_wizard_community));

            }
            else {
                if (args.getString(ARG_OBJECT).equalsIgnoreCase(FRASE_3)) {
                    buttonFragment.setText("Entra nella community!");
                    buttonFragment.setVisibility(View.VISIBLE);
                    frameLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_wizard_wiki));
                    buttonFragment.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity().getBaseContext(), MBAuthenticationActivity.class);
                            startActivity(intent);
                            ((MBApplication) getActivity().getApplication()).setShowWizard(false);
                            getActivity().finish();

				/*
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				           builder.setMessage("Benvenuto nella community!")
				                  .setCancelable(false)
				                  .setTitle("MagicBus")
				                  .setPositiveButton("Accedi", new DialogInterface.OnClickListener() {
				                      public void onClick(DialogInterface dialog, int id) {
				                    	  Intent intent = new Intent(getActivity().getBaseContext(), MBAccediActivity.class);
				                          startActivity(intent);
				  						((MBApplication)getActivity().getApplication()).setShowWizard(false);
				  						getActivity().finish();
				                      }
				                  })
				                  .setNeutralButton("Sono un nuovo utente", new DialogInterface.OnClickListener() {
				                      public void onClick(DialogInterface dialog, int id) {
				                    	  Intent intent = new Intent(getActivity().getBaseContext(), MBRegistrationActivity.class);
				                          startActivity(intent);
				  						((MBApplication)getActivity().getApplication()).setShowWizard(false);
				                      }
				                  }).show();
				 */

                        }
                    });
                    //wizardImage.setImageDrawable(getResources().getDrawable(R.drawable.wiki));

                }
            }

            return rootView;
        }
    }
}
