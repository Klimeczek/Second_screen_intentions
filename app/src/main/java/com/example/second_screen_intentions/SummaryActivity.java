package com.example.second_screen_intentions;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.second_screen_intentions.R;

import java.util.Random;

public class SummaryActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "com.example.second_screen_intentions.EXTRA_NAME";
    public static final String EXTRA_PHONE = "com.example.second_screen_intentions.EXTRA_PHONE";
    public static final String EXTRA_PIZZA = "com.example.second_screen_intentions.EXTRA_PIZZA";
    public static final String EXTRA_SIZE = "com.example.second_screen_intentions.EXTRA_SIZE";
    public static final String EXTRA_QUANTITY = "com.example.second_screen_intentions.EXTRA_QUANTITY";
    public static final String EXTRA_TOPPINGS = "com.example.second_screen_intentions.EXTRA_TOPPINGS";
    public static final String EXTRA_DELIVERY = "com.example.second_screen_intentions.EXTRA_DELIVERY";
    public static final String EXTRA_ADDRESS = "com.example.second_screen_intentions.EXTRA_ADDRESS";
    public static final String EXTRA_PRICE = "com.example.second_screen_intentions.EXTRA_PRICE";

    public static final String RESULT_ORDER_NUMBER = "com.example.second_screen_intentions.RESULT_ORDER_NUMBER";

    private String summaryText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent in = getIntent();
        String name = in.getStringExtra(EXTRA_NAME);
        String phone = in.getStringExtra(EXTRA_PHONE);
        String pizza = in.getStringExtra(EXTRA_PIZZA);
        String size = in.getStringExtra(EXTRA_SIZE);
        int quantity = in.getIntExtra(EXTRA_QUANTITY, 1);
        String toppings = in.getStringExtra(EXTRA_TOPPINGS);
        boolean delivery = in.getBooleanExtra(EXTRA_DELIVERY, false);
        String address = in.getStringExtra(EXTRA_ADDRESS);
        int price = in.getIntExtra(EXTRA_PRICE, 0);

        String deliveryText = delivery
                ? getString(R.string.yes_short) + " (" + address + ")"
                : getString(R.string.no_short);

        summaryText = getString(R.string.summary_format,
                name, phone, pizza, size, quantity, toppings, deliveryText);

        TextView tvSummaryDetails = findViewById(R.id.tvSummaryDetails);
        TextView tvPrice = findViewById(R.id.tvPrice);
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnShare = findViewById(R.id.btnShare);

        tvSummaryDetails.setText(summaryText);
        tvPrice.setText(getString(R.string.price_format, price));

        btnConfirm.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra(RESULT_ORDER_NUMBER, 1000 + new Random().nextInt(9000));
            setResult(RESULT_OK, result);
            finish();
        });

        btnEdit.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
            share.putExtra(Intent.EXTRA_TEXT,
                    summaryText + "\n" + getString(R.string.price_format, price));
            try {
                startActivity(Intent.createChooser(share, getString(R.string.share_chooser)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.error_no_share_app, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }
}