package com.magicmicky.habitrpgmobileapp.userpicture;

import com.magicmicky.habitrpgmobileapp.R;
import com.magicmicky.habitrpgwrapper.lib.models.HabitRPGUser;
import com.magicmicky.habitrpgwrapper.lib.models.Items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class UserPicture {
	
	private Context mContext;
    private Items items;
    private HabitRPGUser user;
	public UserPicture(HabitRPGUser user, Context context) {
		this.user = user;
		this.mContext= context;
	}
	
	
	public Bitmap draw() {

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
		Bitmap imgSprites = BitmapFactory.decodeResource(mContext.getResources(), getSprites(),o);

		Bitmap res = Bitmap.createBitmap(114, 90, Bitmap.Config.ARGB_8888);//114 is the maximum width of the gears.
		Canvas myCanvas = new Canvas(res);
        if(user !=null)
           drawAvatar(user, imgSprites, myCanvas);
        return res;
	}

    private void drawAvatar(HabitRPGUser user, Bitmap imgSprites, Canvas cv) {
        drawSprite(UserSprite.getSkin(user.getPreferences().getSkin()), imgSprites, cv);

//        if(user.getPreferences().getHair() != null) {
//            Log.d("Avatar", "drawing head");
//            drawSprite(UserSprite.gethair_bangs(user.getPreferences().getHair().getBangs(), user.getPreferences().getHair().getColor()), imgSprites, cv);
//            drawSprite(UserSprite.gethair_mustache(user.getPreferences().getHair().getMustache(), user.getPreferences().getHair().getColor()), imgSprites, cv);
//            drawSprite(UserSprite.gethair_base(user.getPreferences().getHair().getBase(), user.getPreferences().getHair().getColor()), imgSprites, cv);
//            drawSprite(UserSprite.gethair_beard(user.getPreferences().getHair().getBeard(), user.getPreferences().getHair().getColor()), imgSprites, cv);
//        }
//
//        UserLook.UserItems items = user.getPreferences().isCostume() ? user.getItems().getGear() : user.getItems();
//        drawSprite(UserSprite.getArmor(items.getArmor(), user.getSize(), user.getPreferences().getShirt()), imgSprites, cv);
//        drawSprite(UserSprite.getWeapon(items.getWeapon()),imgSprites,cv);
//        drawSprite(UserSprite.getShield(items.getShield()),imgSprites,cv);
//        drawSprite(UserSprite.getHead(items.getHead()),imgSprites,cv);
    }

    private void drawSprite(UserObject obj, Bitmap img, Canvas cv) {
        if(obj!=null)
            modifyCanvas(img,(-1)*obj.getX(),(-1)*obj.getY(),obj.getWidth(), obj.getHeight(),cv);
        else
            Log.e("Avatar", "obj draw sprite = null");
    }

	private int getSprites() {
		return R.drawable.spritesmith;
	}

	private void modifyCanvas(Bitmap img, int start_x, int start_y,int width, int height, Canvas canvas) {
		Rect source = new Rect(start_x,start_y, start_x+width, start_y +height);
		Rect dest = new Rect(0,0,width, height);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        canvas.drawBitmap(img, source, dest, paint);

	}

	/**
	 * From StackOverflow
	 * find the rect value to scale the image (delete the transparent side)
	 * @param image the image to search in
	 * @return the x1,x2,y1,y2 to form the new Rect(res[0],res[2],res[1],res[3])
	 */
	private int[] findRectValues(Bitmap image)
	{
		int[] res = {0,0,0,0};//x1,x2,y1,y2

		for(int x = 0; x < image.getWidth(); x++){
			for(int y = 0; y < image.getHeight(); y++){
				if(image.getPixel(x, y) != Color.TRANSPARENT){
					res[0] = x;
					break;
				}
			}
			if(res[0] != 0)
				break;
		}
		for(int x = image.getWidth()-1; x > 0; x--)	{
			for(int y = 0; y < image.getHeight(); y++){
				if(image.getPixel(x, y) != Color.TRANSPARENT){
					res[1] = x;
					break;
				}
			}
			if(res[1] != 0)
				break;
		}
		for(int y = 0; y < image.getHeight(); y++){
			for(int x = 0; x < image.getWidth(); x++){
				if(image.getPixel(x, y) != Color.TRANSPARENT){
					res[2] = y;
					break;
				}
			}
			if(res[2] != 0)
				break;
		}
		for(int y = image.getHeight()-1; y > 0; y--){
			for(int x = 0; x < image.getWidth(); x++){
				if(image.getPixel(x, y) != Color.TRANSPARENT){
					res[3] = y;
					break;
				}
			}
			if(res[3] != 0)
				break;
		}
		return res;
	}

}