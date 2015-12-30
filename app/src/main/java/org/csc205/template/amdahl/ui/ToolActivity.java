/*
 * Copyright (c) 2015,2016 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.csc205.template.amdahl.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.csc205.template.amdahl.R;
import org.csc205.template.amdahl.engine.Amdahl;

public class ToolActivity extends AppCompatActivity {

    private Fragment fragment;
    private int fragmentId;

    //OnCreate Method:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool);

        fragmentId = R.id.activityTool_container;

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo_template);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        // Check if fragment already exists
        FragmentManager fm = getFragmentManager();
        fragment = fm.findFragmentById(fragmentId);
        if (fragment == null) {
            fragment = GraphFragment.newInstance(Amdahl.type_default);

            fm.beginTransaction().add(fragmentId, fragment).commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_info: {
                FragmentManager fm = getFragmentManager();
                InfoDialogFragment dialogFragment;
                dialogFragment = InfoDialogFragment.newInstance(R.raw.info);
                dialogFragment.show(fm, InfoDialogFragment.dialogTag);
                return true;
            }
            case R.id.menu_eula: {
                FragmentManager fm = getFragmentManager();
                InfoDialogFragment dialogFragment;
                dialogFragment = InfoDialogFragment.newInstance(R.raw.eula);
                dialogFragment.show(fm, InfoDialogFragment.dialogTag);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
