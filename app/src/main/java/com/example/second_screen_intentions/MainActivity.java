package com.example.second_screen_intentions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.second_screen_intentions.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "STOS";

    private static final int[] BASE_PRICES = {0, 25, 29, 30, 33, 31};
    private static final int PRICE_MEDIUM = 6;
    private static final int PRICE_LARGE = 12;
    private static final int PRICE_TOPPING = 4;
    private static final int PRICE_DELIVERY = 8;

    private EditText etName, etPhone, etAddress;
    private Spinner spPizza;
    private RadioGroup rgSize;
    private SeekBar sbQuantity;
    private TextView tvQuantityValue;
    private CheckBox cbCheese, cbPepperoni, cbMushrooms, cbTerms;
    private MaterialSwitch swDelivery;
    private Button btnSubmit, btnClear, btnAbout;

    private String savedName = "";
    private String savedPhone = "";
    private String savedAddress = "";
    private int savedPizzaPosition = 0;
    private int savedSizeId = -1;
    private int savedQuantity = 1;
    private boolean savedCheese = false;
    private boolean savedPepperoni = false;
    private boolean savedMushrooms = false;
    private boolean savedDelivery = false;
    private boolean savedTerms = false;

    private final ActivityResultLauncher<Intent> summaryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    int orderNumber = 0;
                    Intent data = result.getData();
                    if (data != null) {
                        orderNumber = data.getIntExtra(SummaryActivity.RESULT_ORDER_NUMBER, 0);
                    }
                    resetFields();
                    Snackbar.make(findViewById(R.id.main),
                            getString(R.string.order_confirmed, orderNumber),
                            Snackbar.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.order_cancelled, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity onCreate");
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        spPizza = findViewById(R.id.spPizza);
        rgSize = findViewById(R.id.rgSize);
        sbQuantity = findViewById(R.id.sbQuantity);
        tvQuantityValue = findViewById(R.id.tvQuantityValue);
        cbCheese = findViewById(R.id.cbCheese);
        cbPepperoni = findViewById(R.id.cbPepperoni);
        cbMushrooms = findViewById(R.id.cbMushrooms);
        cbTerms = findViewById(R.id.cbTerms);
        swDelivery = findViewById(R.id.swDelivery);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClear = findViewById(R.id.btnClear);
        btnAbout = findViewById(R.id.btnAbout);

        setUpPizzaSpinner();
        setUpListeners();
        tvQuantityValue.setText(String.valueOf(sbQuantity.getProgress()));
    }

    private void setUpPizzaSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.pizzas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPizza.setAdapter(adapter);
    }

    private void setUpListeners() {
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) ->
                btnSubmit.setEnabled(isChecked));

        swDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etAddress.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                etAddress.setText("");
                etAddress.setError(null);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                openSummary();
            }
        });

        btnClear.setOnClickListener(v -> askForClearConfirmation());

        btnAbout.setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        sbQuantity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    return;
                }
                tvQuantityValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private boolean validateForm() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError(getString(R.string.error_name));
            etName.requestFocus();
            return false;
        }

        String phone = etPhone.getText().toString().trim();
        if (phone.replace(" ", "").length() < 9) {
            etPhone.setError(getString(R.string.error_phone));
            etPhone.requestFocus();
            return false;
        }

        if (spPizza.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.error_pizza, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgSize.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.error_size, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (swDelivery.isChecked()) {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                etAddress.setError(getString(R.string.error_address));
                etAddress.requestFocus();
                return false;
            }
        }

        return true;
    }

    private String getSizeText() {
        int checkedId = rgSize.getCheckedRadioButtonId();
        if (checkedId == R.id.rbSmall) {
            return getString(R.string.size_small);
        } else if (checkedId == R.id.rbMedium) {
            return getString(R.string.size_medium);
        }
        return getString(R.string.size_large);
    }

    private String getToppingsText() {
        List<String> list = new ArrayList<>();
        if (cbCheese.isChecked()) list.add(getString(R.string.topping_cheese));
        if (cbPepperoni.isChecked()) list.add(getString(R.string.topping_pepperoni));
        if (cbMushrooms.isChecked()) list.add(getString(R.string.topping_mushrooms));
        if (list.isEmpty()) {
            return getString(R.string.no_toppings);
        }
        return String.join(", ", list);
    }

    private int calculatePrice() {
        int unit = BASE_PRICES[spPizza.getSelectedItemPosition()];

        int checkedId = rgSize.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMedium) {
            unit += PRICE_MEDIUM;
        } else if (checkedId == R.id.rbLarge) {
            unit += PRICE_LARGE;
        }

        if (cbCheese.isChecked()) unit += PRICE_TOPPING;
        if (cbPepperoni.isChecked()) unit += PRICE_TOPPING;
        if (cbMushrooms.isChecked()) unit += PRICE_TOPPING;

        int total = unit * sbQuantity.getProgress();
        if (swDelivery.isChecked()) total += PRICE_DELIVERY;
        return total;
    }

    private void openSummary() {
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra(SummaryActivity.EXTRA_NAME, etName.getText().toString().trim());
        intent.putExtra(SummaryActivity.EXTRA_PHONE, etPhone.getText().toString().trim());
        intent.putExtra(SummaryActivity.EXTRA_PIZZA, spPizza.getSelectedItem().toString());
        intent.putExtra(SummaryActivity.EXTRA_SIZE, getSizeText());
        intent.putExtra(SummaryActivity.EXTRA_QUANTITY, sbQuantity.getProgress());
        intent.putExtra(SummaryActivity.EXTRA_TOPPINGS, getToppingsText());
        intent.putExtra(SummaryActivity.EXTRA_DELIVERY, swDelivery.isChecked());
        intent.putExtra(SummaryActivity.EXTRA_ADDRESS, etAddress.getText().toString().trim());
        intent.putExtra(SummaryActivity.EXTRA_PRICE, calculatePrice());
        summaryLauncher.launch(intent);
    }

    private void askForClearConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_title)
                .setMessage(R.string.clear_message)
                .setPositiveButton(R.string.clear_yes, (dialog, which) -> clearForm())
                .setNegativeButton(R.string.clear_cancel, null)
                .show();
    }

    private void saveFormState() {
        savedName = etName.getText().toString();
        savedPhone = etPhone.getText().toString();
        savedAddress = etAddress.getText().toString();
        savedPizzaPosition = spPizza.getSelectedItemPosition();
        savedSizeId = rgSize.getCheckedRadioButtonId();
        savedQuantity = sbQuantity.getProgress();
        savedCheese = cbCheese.isChecked();
        savedPepperoni = cbPepperoni.isChecked();
        savedMushrooms = cbMushrooms.isChecked();
        savedDelivery = swDelivery.isChecked();
        savedTerms = cbTerms.isChecked();
    }

    private void resetFields() {
        etName.setText("");
        etName.setError(null);

        etPhone.setText("");
        etPhone.setError(null);

        etAddress.setText("");
        etAddress.setError(null);
        etAddress.setVisibility(View.GONE);

        spPizza.setSelection(0);
        rgSize.clearCheck();

        sbQuantity.setProgress(1);
        tvQuantityValue.setText("1");

        cbCheese.setChecked(false);
        cbPepperoni.setChecked(false);
        cbMushrooms.setChecked(false);

        swDelivery.setChecked(false);

        cbTerms.setChecked(false);
        btnSubmit.setEnabled(false);
    }

    private void clearForm() {
        saveFormState();
        resetFields();

        Snackbar.make(findViewById(R.id.main), R.string.cleared, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> restoreFormState())
                .show();
    }

    private void restoreFormState() {
        etName.setText(savedName);
        etPhone.setText(savedPhone);
        etAddress.setText(savedAddress);

        spPizza.setSelection(savedPizzaPosition);

        if (savedSizeId != -1) {
            rgSize.check(savedSizeId);
        } else {
            rgSize.clearCheck();
        }

        sbQuantity.setProgress(savedQuantity);
        tvQuantityValue.setText(String.valueOf(savedQuantity));

        cbCheese.setChecked(savedCheese);
        cbPepperoni.setChecked(savedPepperoni);
        cbMushrooms.setChecked(savedMushrooms);

        swDelivery.setChecked(savedDelivery);
        etAddress.setVisibility(savedDelivery ? View.VISIBLE : View.GONE);

        cbTerms.setChecked(savedTerms);
        btnSubmit.setEnabled(savedTerms);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy");
    }
}