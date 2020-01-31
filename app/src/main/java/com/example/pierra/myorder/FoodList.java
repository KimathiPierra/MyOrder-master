    package com.example.pierra.myorder;

    import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pierra.myorder.Common.Common;
import com.example.pierra.myorder.Interface.ItemClickListener;
import com.example.pierra.myorder.Model.Food;
import com.example.pierra.myorder.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;
    import com.mancj.materialsearchbar.MaterialSearchBar;
    import com.squareup.picasso.Picasso;

    import java.util.ArrayList;
    import java.util.List;

    public class FoodList extends AppCompatActivity {

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;

        FirebaseDatabase database;
        DatabaseReference foodList;

        String catergoryId="";
        FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

//Search function
       FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
        List<String> suggestList = new ArrayList<>();
        MaterialSearchBar materialSearchBar;



        private static final String TAG = "FoodList";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_food_list);

            //Firebase
            database = FirebaseDatabase.getInstance();
            foodList = database.getReference("Foods");

            recyclerView = (RecyclerView)findViewById(R.id.recycler_food);
            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            //Get Intent Here
            if (getIntent() != null) {
                catergoryId = getIntent().getStringExtra("CategoryId");
            } if (!catergoryId.isEmpty() && catergoryId != null)
            {
                if (Common.isConnectedToInternet(getBaseContext()))
                loadListFood(catergoryId);
                else
                {
                    Toast.makeText(FoodList.this, "Please check you internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

           materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
            materialSearchBar.setHint("Enter your food");

            loadSuggest();

            materialSearchBar.setLastSuggestions(suggestList);
            materialSearchBar.setCardViewElevation(10);
            materialSearchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                         List<String> suggest = new ArrayList<String>();
                    for (String search:suggestList)
                    {
                        if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                            suggest.add(search);
                    }
                    materialSearchBar.setLastSuggestions(suggest);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {
                    if (!enabled)
                        recyclerView.setAdapter(adapter);
                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                     startSearch(text);
                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });
        }

        private void startSearch(CharSequence text) {
            searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                    Food.class,
                    R.layout.food_item,
                    FoodViewHolder.class,
                    foodList.orderByChild("name").equalTo(text.toString())
            ) {
                @Override
                protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                    viewHolder.food_name.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImage())
                            .into(viewHolder.food_image);

                    final Food local = model;
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                            {
                                //Start New Activity
                                Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                                foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());    //send food id to new activity
                                startActivity(foodDetail);
                            }
                        }
                    });
                }
            };
            recyclerView.setAdapter(searchAdapter);
        }

        private void loadSuggest() {
            foodList.orderByChild("menuId").equalTo(catergoryId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                            {
                                Food item = postSnapshot.getValue(Food.class);
                                suggestList.add(item.getName());
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }


        private void loadListFood(String catergoryId) {
            Log.d(TAG, "loadListFood: Start");
            adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                    R.layout.food_item,
                    FoodViewHolder.class,
                    foodList.orderByChild("menuId").equalTo(catergoryId)) {
                @Override
                protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                    viewHolder.food_name.setText(model.getName());
                    Picasso.with(getBaseContext()).load(model.getImage())
                            .into(viewHolder.food_image);

                    final Food local = model;
                    viewHolder.setItemClickListener(new ItemClickListener() {
                        @Override
                        public void onClick(View view, int position, boolean isLongClick) {
                            Toast.makeText(FoodList.this, ""+local.getName(), Toast.LENGTH_SHORT).show();
                            {
                                //Start New Activity
                                Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                                foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());    //send food id to new activity
                                startActivity(foodDetail);
                            }
                        }
                    });
                }
            };
            //set Adapter
            recyclerView.setAdapter(adapter);
        }
    }
