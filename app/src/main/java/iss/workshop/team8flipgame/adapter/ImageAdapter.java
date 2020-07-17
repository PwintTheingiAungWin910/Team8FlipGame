package iss.workshop.team8flipgame.adapter;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

import iss.workshop.team8flipgame.activity.GameActivity;
import iss.workshop.team8flipgame.R;
import iss.workshop.team8flipgame.activity.ImagePickingActivity;
import iss.workshop.team8flipgame.model.Image;
import iss.workshop.team8flipgame.model.Score;
import iss.workshop.team8flipgame.service.DBService;

public class ImageAdapter extends BaseAdapter{

    private  Context mContext;
    private  ArrayList<Image> images;
    public ArrayList<Bitmap> barray = new ArrayList<>();
    ArrayList<ImageView> seleted_view = new ArrayList<>();
    boolean disableFlip;

    private static final int NUM_OF_CARDS = 6;
    int numOfAttempts = 0;
    int totalTime = 0;

    private static int MASK_HINT_COLOR = 0x99ffffff;
    public ImageAdapter(Context mContext, ArrayList<Image> images){
        this.mContext = mContext;
        this.images=images;
    }

    @Override
    public int getCount(){
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        if(mContext instanceof ImagePickingActivity){

            final Image image = images.get(pos);
            image.setPosID(pos);
            if (view == null) {
                final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                view = layoutInflater.inflate(R.layout.images, null);
            }
            final ImageView imageView1 = view.findViewById(R.id.image);
            imageView1.setImageBitmap(image.getBitmap());

            imageView1.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {


                    System.out.println("Image picking: " + image.getPosID());
                    System.out.println(image.getPosID());
                    if (imageView1.getColorFilter() == null) {
                        //Log.i("IMAGE_TEST","color filter on ");
                        imageView1.setColorFilter(MASK_HINT_COLOR, PorterDuff.Mode.SRC_OVER);
                    }
                    else imageView1.clearColorFilter();

                    if(ImagePickingActivity.selectedCell.contains(Integer.valueOf(image.getPosID())))
                    { ImagePickingActivity.selectedCell.remove(Integer.valueOf(image.getPosID()));
                        ImagePickingActivity.listen.setValue(ImagePickingActivity.selectedCell.size());}
                    else{ImagePickingActivity.selectedCell.add(image.getPosID());
                        ImagePickingActivity.listen.setValue(ImagePickingActivity.selectedCell.size());}


                    if (ImagePickingActivity.selectedCell.size()==ImagePickingActivity.gameImageNo){
                        Intent intent = new Intent(mContext, GameActivity.class);
                        intent.putExtra("selectedCells",ImagePickingActivity.selectedCell);
                        intent.putExtra("IS_MUTED",ImagePickingActivity.IS_MUTED);
                        mContext.startActivity(intent);
                    }
                }
            });
        }

        if(mContext instanceof GameActivity){

            final Image image = images.get(pos);
            image.setPosID(pos);

            if (view == null) {
                final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                view = layoutInflater.inflate(R.layout.images2, null);
            }

            final ImageView imageView2 =view.findViewById(R.id.image2);
            //imageView2.setImageBitmap(image.getBitmap());

            //Flipper View instantiate
            final ViewFlipper flipper = view.findViewById(R.id.my_view_flipper);

            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    seleted_view.get(1).setImageBitmap(null);
                    seleted_view.get(1).setClickable(true);
                    seleted_view.get(0).setClickable(true);
                    seleted_view.get(0).setImageBitmap(null);
                    barray.clear();
                    seleted_view.clear();
                }
            };

            imageView2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    System.out.println("Game Activity " + image.getPosID());
                    System.out.println(image.getBitmap());
                    ((ImageView) view).setImageBitmap(image.getBitmap());

                    flipViewFlipper(flipper);

                    if(barray.size()<2){

                        System.out.println("pos1");
                        Bitmap b = image.getBitmap();
                        view.setClickable(false);
                        barray.add(b);
                        seleted_view.add((ImageView) view);
                    }

                    if(barray.size()==2){
                        System.out.println("pos2");
                        if(barray.get(0) == barray.get(1)){
                            System.out.println("pos2.1");
                            System.out.println("same");
                            view.setClickable(false);
                            seleted_view.get(0).setClickable(false);
                            barray.clear();
                            seleted_view.clear();
                        }
                        else{
                            System.out.println("pos2.2");
                            System.out.println("not same");
                            handler.postDelayed(runnable,300);
                        }
                    }
                }



            });
        }
        return view;
    }

    public int calculateScore(int totalTime,int numOfAttempts){
        return (5 * NUM_OF_CARDS) + (500 / numOfAttempts) + (5000 / totalTime);
    }

    public void finishedGame(int totalTime,int numOfAttempts){
        int totalScore = calculateScore(60,15);
        Score scoreObj = new Score("Theingi",totalScore);
        DBService db = new DBService(mContext);
        db.addScore(scoreObj);
    }

    public void updateItemList(ArrayList<Image> newItemList) {
        this.images = newItemList;
        notifyDataSetChanged();
    }

    //Flipper method
    private void flipViewFlipper(ViewFlipper flipper){
        if(flipper.getDisplayedChild() == 0){
            flipper.setDisplayedChild(1);
        }
        else{
            flipper.setDisplayedChild(0);
        }
    }

}
