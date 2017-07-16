package com.example.arpityadav.bookstore;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arpityadav.bookstore.Model.Example;
import com.example.arpityadav.bookstore.Model.ImageLinks;
import com.example.arpityadav.bookstore.Model.Item;
import com.example.arpityadav.bookstore.Model.VolumeInfo;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private Button scanBtn, previewBtn, linkBtn;
    private LinearLayout starLayout;
    private ImageView thumbView;
    private ImageView[] starViews;
    private ApiInterface apiInterface;
    private String key="AIzaSyA-vw58hcGEBsbc3-duGRF0PgkzZ9oVcEo";
    private String publish= "Published Date:";

    private TextView authorText, titleText, descriptionText, dateText, ratingCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanBtn = (Button)findViewById(R.id.scan_button);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                scanIntegrator.initiateScan();
            }
        });
        previewBtn = (Button)findViewById(R.id.preview_btn);
        previewBtn.setVisibility(View.GONE);

        linkBtn = (Button)findViewById(R.id.link_btn);
        linkBtn.setVisibility(View.GONE);
        linkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        linkBtn=(Button)findViewById(R.id.link_btn);
        authorText = (TextView)findViewById(R.id.book_author);
        titleText = (TextView)findViewById(R.id.book_title);
        descriptionText = (TextView)findViewById(R.id.book_description);
        dateText = (TextView)findViewById(R.id.book_date);
        starLayout = (LinearLayout)findViewById(R.id.star_layout);
        ratingCountText = (TextView)findViewById(R.id.book_rating_count);
        thumbView = (ImageView)findViewById(R.id.thumb);
        starViews=new ImageView[5];
        for(int s=0; s<starViews.length; s++){
            starViews[s]=new ImageView(this);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //check we have a valid result
        if (scanningResult != null) {
            //get content from Intent Result
            final String scanContent = scanningResult.getContents();

            //get format name of data scanned
            String scanFormat = scanningResult.getFormatName();
            if(scanContent!=null && scanFormat!=null && scanFormat.equalsIgnoreCase("EAN_13")){
                //String bookSearchString = "https://www.googleapis.com/books/v1/volumes?"+
                      //  "q=isbn:"+scanContent+"&key=AIzaSyA-vw58hcGEBsbc3-duGRF0PgkzZ9oVcEo";
                apiInterface= ApiClient.getClient().create(ApiInterface.class);
                Call<Example> call = apiInterface.getBook(scanContent,key);
                call.enqueue(new Callback<Example>() {
                    @Override
                    public void onResponse(Call<Example> call, Response<Example> response) {
                        previewBtn.setVisibility(View.VISIBLE);
                        List<Item> items= response.body().getItems();
                        Item item= items.get(0);
                        VolumeInfo volumeInfo= item.getVolumeInfo();
                        titleText.setText(volumeInfo.getTitle());
                        List<String> authors=volumeInfo.getAuthors();
                        StringBuilder builder = new StringBuilder();
                        for (String details : authors) {
                            builder.append(details + "  ");
                        }
                        authorText.setText(builder.toString());
                        dateText.setText(publish+ " "+volumeInfo.getPublishedDate());
                        descriptionText.setText( volumeInfo.getDescription());
                        double decNum= Double.parseDouble(" " + volumeInfo.getAverageRating());

                        int numstar= (int) decNum ;
                        starLayout.setTag(numstar);
                        starLayout.removeAllViews();
                        if(numstar<0) {
                        }
                        else {
                            for (int s = 0; s < numstar; s++) {
                                starViews[s].setImageResource(R.drawable.star);
                                starLayout.addView(starViews[s]);
                            }
                        }

                        ratingCountText.setText( " " + volumeInfo.getRatingsCount());
                        ImageLinks img = volumeInfo.getImageLinks();

                        Picasso.with(MainActivity.this).load(img.getSmallThumbnail()).into(thumbView);
                        linkBtn.setTag(volumeInfo.getInfoLink());
                        linkBtn.setVisibility(View.VISIBLE);
                        linkBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String tag = (String)linkBtn.getTag();
                                //launch the url
                                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                                webIntent.setData(Uri.parse(tag));
                                startActivity(webIntent);
                            }
                        });
                        previewBtn.setTag(scanContent);
                        previewBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String tag =(String) previewBtn.getTag();
                                Intent intent = new Intent(MainActivity.this, EmbeddedBook.class);
                                intent.putExtra("isbn", tag);
                                startActivity(intent);
                            }
                        });

                    }

                    @Override
                    public void onFailure(Call<Example> call, Throwable t) {

                    }
                });
            }
            else{
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Not a valid scan!", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
        else{
            //invalid scan data or scan canceled
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No book scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }
}
