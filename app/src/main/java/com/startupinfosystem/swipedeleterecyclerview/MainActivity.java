package com.startupinfosystem.swipedeleterecyclerview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.startupinfosystem.swipedeleterecyclerview.adapters.MenuAdapter;
import com.startupinfosystem.swipedeleterecyclerview.api.ApiClient;
import com.startupinfosystem.swipedeleterecyclerview.api.ApiInterface;
import com.startupinfosystem.swipedeleterecyclerview.model.MenuModel;
import com.startupinfosystem.swipedeleterecyclerview.utils.RecyclerItemTouchHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private RecyclerView rvMenu;
    private ApiInterface apiInterface;
    private List<MenuModel> menuModelList = new ArrayList<>();
    private MenuAdapter menuAdapter;
    private CoordinatorLayout coordinator_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinator_layout = findViewById(R.id.coordinator_layout);
        rvMenu = findViewById(R.id.rvMenu);
        rvMenu.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getMenuData();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvMenu);

    }

    private void getMenuData() {
        Call<ResponseBody> call = apiInterface.getMenu();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String data = response.body().string();

                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        MenuModel menuModel = new MenuModel();
                        menuModel.setId(Integer.parseInt(jsonObject.getString("id")));
                        menuModel.setName(jsonObject.getString("name"));
                        menuModel.setDescription(jsonObject.getString("description"));
                        menuModel.setPrice(jsonObject.getString("price"));
                        menuModel.setThumb(jsonObject.getString("thumbnail"));
                        menuModelList.add(menuModel);
                    }
                    menuAdapter = new MenuAdapter(getApplicationContext(), menuModelList);
                    rvMenu.setAdapter(menuAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "failed.. .to load", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof MenuAdapter.MyViewHolder) {
            // get the removed item name to display it in snack bar
            String name = menuModelList.get(viewHolder.getAdapterPosition()).getName();

            // backup of removed item for undo purpose
            final MenuModel deletedItem = menuModelList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            menuAdapter.removeItem(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(coordinator_layout, name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    menuAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}