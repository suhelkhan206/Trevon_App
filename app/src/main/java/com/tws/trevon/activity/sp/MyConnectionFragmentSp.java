package com.tws.trevon.activity.sp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tws.trevon.R;
import com.tws.trevon.adapter.MyConnectionAdapter;
import com.tws.trevon.co.ImageCO;
import com.tws.trevon.co.UserCO;
import com.tws.trevon.common.AppController;
import com.tws.trevon.common.CallApi;
import com.tws.trevon.constants.API;
import com.tws.trevon.fragment.AbstractFragment;
import com.tws.trevon.fragment.FragmentImageSlider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyConnectionFragmentSp extends AbstractFragment {

    RecyclerView my_connection_recyclreview;
    MyConnectionAdapter myConnectionAdapter;
    public List<UserCO> userCOList = new ArrayList<>();

    CircleImageView user_image;
    TextView user_name,user_phone_number, wallet_amount;
    LinearLayout ll_select_user_profile;
    ImageView crown;
    LinearLayout no_connection_found;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_my_connection_sp, container, false);

        crown = view.findViewById(R.id.crown);
        if(UserCO.getUserCOFromDB().is_premium.equals("1"))
        {
            crown.setVisibility(View.VISIBLE);
        }
        else
        {
            crown.setVisibility(View.GONE);
        }

        ll_select_user_profile = view.findViewById(R.id.ll_select_user_profile);
        ll_select_user_profile.setVisibility(View.GONE);

        user_image = view.findViewById(R.id.user_image);
        user_name = view.findViewById(R.id.user_name);
        user_phone_number = view.findViewById(R.id.user_phone_number);
        wallet_amount = view.findViewById(R.id.wallet_amount);
        no_connection_found = view.findViewById(R.id.no_connection_found);
        user_name.setText(UserCO.getUserCOFromDB().username);
        user_phone_number.setText(UserCO.getUserCOFromDB().mobile);
        user_image.setOnClickListener(this);
        wallet_amount.setText(UserCO.getUserCOFromDB().wallet_amount);
        Glide.with(getActivity())
                .load(UserCO.getUserCOFromDB().image)
                .apply(RequestOptions.circleCropTransform().placeholderOf(R.drawable.error_images).error(R.drawable.error_images))
                .dontAnimate()
                .into(user_image);

        my_connection_recyclreview = view.findViewById(R.id.my_connection_recyclreview);

        myConnectionAdapter = new MyConnectionAdapter(getActivity(), userCOList);
        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getActivity());
        my_connection_recyclreview.setLayoutManager(mLayoutManager2);
        my_connection_recyclreview.setAdapter(myConnectionAdapter);


        CallApi callApi = new CallApi(API.my_connection);
        callApi.addReqParams("user_id", UserCO.getUserCOFromDB().id);//
        processCallApi(callApi);

        myConnectionAdapter.setClickListener(new MyConnectionAdapter.ClickListner()
        {
            @Override
            public void onitemClick(View view, int position)
            {
                Intent i = new Intent(getActivity(), MyOrderListSp.class);
                i.putExtra("user_id",userCOList.get(position).id);
                startActivity(i);
            }
        });

        return view;
    }

    @Override
    protected void onViewClick(View view)
    {
        switch(view.getId()) {
            case R.id.user_image:
                List<ImageCO> imageCOList = new ArrayList<>();
                ImageCO imageCO = new ImageCO();
                imageCO.url = UserCO.getUserCOFromDB().image;
                imageCO.type = "image";
                imageCOList.add(imageCO);

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("images", new ArrayList(imageCOList));
                bundle.putString("isPdfShow","no");
                bundle.putInt("position", 0);
                bundle.putInt("type",2);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                FragmentImageSlider newFragment = FragmentImageSlider.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
                break;
        }


    }

    @Override
    protected void onApiCallSuccess(Object responseValues, CallApi callApi)
    {
        if(API.my_connection.method.equals(callApi.networkActivity.method))
        {
            try {
                JSONObject jsonObject = new JSONObject(responseValues.toString());
                JSONObject action = jsonObject.getJSONObject("response");
                String nessage = action.getString("message");
                String status = action.getString("status");
                if (status.equalsIgnoreCase("Success"))
                {
                    userCOList.clear();
                    JSONArray onbjnew = action.getJSONArray("data");
                    for(int i = 0 ; i<onbjnew.length();i++)
                    {
                        JSONObject jsonObject1 = onbjnew.getJSONObject(i);
                        UserCO userCO = AppController.gson.fromJson(jsonObject1.toString(), UserCO.class);
                        userCOList.add(userCO);
                    }
                    if(userCOList.size() > 0)
                    {
                        no_connection_found.setVisibility(View.GONE);
                    }
                    else
                    {
                        no_connection_found.setVisibility(View.VISIBLE);
                    }

                    myConnectionAdapter.userCOList = userCOList;
                    myConnectionAdapter.notifyDataSetChanged();
                } else {
                    no_connection_found.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), nessage, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                no_connection_found.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }

        }
    }


}