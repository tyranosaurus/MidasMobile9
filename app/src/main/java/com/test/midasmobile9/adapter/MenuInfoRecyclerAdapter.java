package com.test.midasmobile9.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.test.midasmobile9.R;
import com.test.midasmobile9.activity.AdminActivity;
import com.test.midasmobile9.activity.MenuEditActivity;
import com.test.midasmobile9.data.AdminCoffeeOrderItem;
import com.test.midasmobile9.data.AdminMenuItem;
import com.test.midasmobile9.model.MenuInfoModel;
import com.test.midasmobile9.network.NetworkDefineConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuInfoRecyclerAdapter extends RecyclerView.Adapter<MenuInfoRecyclerAdapter.MenuViewHolder> {
    public static final String PROFILE_URL_HEADER = NetworkDefineConstant.HOST_URL + "/menuimg/";

    Context context = null;
    RecyclerView.LayoutManager layoutManager = null;

    List<AdminMenuItem> items = new ArrayList<>();

    public MenuInfoRecyclerAdapter(Context context, RecyclerView.LayoutManager layoutManager) {
        this.context = context;
        this.layoutManager = layoutManager;
    }

    @NonNull
    @Override
    public MenuInfoRecyclerAdapter.MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coffee_menu, parent, false);

        return new MenuViewHolder(parent.getContext(), view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuInfoRecyclerAdapter.MenuViewHolder holder, int position) {
        AdminMenuItem item = items.get(position);

        // 커피 이미지
        Glide.with(context)
                .load(PROFILE_URL_HEADER + item.getImg())
                .into(holder.imageViewCoffeeImage);
        // 커피 이름
        holder.textViewCoffeeMenuName.setText(item.getName());
        // 커피 가격
        String price = item.getPrice() + " 원";
        holder.textViewCoffeeMenuPrice.setText(price);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(AdminMenuItem item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
    }

    public void addNewItem(AdminMenuItem item) {
        items.add(0, item);

        // 애니메이션
        notifyItemInserted(0);

        layoutManager.scrollToPosition(0);
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder {
        Context context = null;
        View view = null;

        @BindView(R.id.imageViewCoffeeImage)
        ImageView imageViewCoffeeImage;
        @BindView(R.id.textViewCoffeeMenuName)
        TextView textViewCoffeeMenuName;
        @BindView(R.id.textViewCoffeeMenuPrice)
        TextView textViewCoffeeMenuPrice;
        @BindView(R.id.imageViewCoffeeMenuPopMenu)
        ImageView imageViewCoffeeMenuPopMenu;

        public MenuViewHolder(Context context, View itemView) {
            super(itemView);

            // 버터나이프
            ButterKnife.bind(this, itemView);

            if ( context != null ) {
                this.context = context;
            }

            if ( itemView != null ) {
                this.view = itemView;
            }
        }

        @OnClick(R.id.imageViewCoffeeMenuPopMenu)
        public void onClickCoffeeMenuPopMenu(View view) {
            // 팝업메뉴 생성(두번째 인자 : 팝업메뉴가 보여질 앵커)
            PopupMenu popupMenu = new PopupMenu(context, imageViewCoffeeMenuPopMenu);
            // 팝업메뉴 인플레이션
            ((AdminActivity)context).getMenuInflater().inflate(R.menu.menu_admin_manage_popup, popupMenu.getMenu());
            // 팝업메뉴 클릭 이벤트 처리
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AdminMenuItem selectedItem = items.get(getAdapterPosition());
                    final int no = selectedItem.getNo();
                    final int position = getAdapterPosition();

                    switch ( item.getItemId() ) {
                        case R.id.popup_menu_modify:
                            // 메뉴 수정 액티비티 띄우기
                            Intent intent = new Intent((context), MenuEditActivity.class);
                            intent.putExtra("editMenuItem", selectedItem);
                            context.startActivity(intent);

                            break;
                        case R.id.popup_menu_delete:
                            AlertDialog.Builder ab = new AlertDialog.Builder(context);
                            ab.setMessage("메뉴를 삭제하시겠습니까?");
                            ab.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new MenuDeleteTask().execute(no, position);
                                }
                            });
                            ab.setNegativeButton("취소", null);
                            ab.show();

                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            // 팝업메뉴 보이기
            popupMenu.show();
        }
    }

    public class MenuDeleteTask extends AsyncTask<Integer, Void, Map<String, Object>> {
        int position = -1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Map<String, Object> doInBackground(Integer... params) {
            int no = params[0];
            position = params[1];

            Map<String, Object> map = MenuInfoModel.deleteMenu(no);
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, Object> map) {
            super.onPostExecute(map);

            if ( map == null ) {
                String message = "인터넷 연결이 원활하지 않습니다. 잠시후 다시 시도해주세요.";
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } else {
                // 통신성공
                boolean result = false;
                String message = null;

                if ( map.containsKey("result") ) {
                    result = (boolean)map.get("result");
                }

                if ( map.containsKey("message") ) {
                    message = (String)map.get("message");
                }

                if ( result ) {
                    // 메뉴 삭제 성공
                    Toast.makeText(context, "메뉴가 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                    notifyItemRemoved(position);
                    items.remove(position);
                } else {
                    // 메뉴 삭제 실패
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                }
            }
        }
    }
}
