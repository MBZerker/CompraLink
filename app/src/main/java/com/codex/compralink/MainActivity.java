package com.codex.compralink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.res.Configuration;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class MainActivity extends Activity {
    private static final String PREFS = "compralink";
    private static final String KEY_LISTS = "lists";
    private static final String KEY_THEME = "theme_mode";
    private static final int THEME_SYSTEM = 0;
    private static final int THEME_LIGHT = 1;
    private static final int THEME_DARK = 2;
    private static final String SHARE_BASE = "https://compralink.app/l/";
    private static final String OLD_SHARE_PREFIX = "https://compralink.app/list?payload=";
    private static final String CUSTOM_SHARE_PREFIX = "compralink://list?payload=";

    private final List<ShoppingList> lists = new ArrayList<>();
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private LinearLayout root;
    private EditText itemInput;
    private EditText priceInput;
    private int selectedIndex = -1;
    private boolean shellReady;
    private boolean pendingIntentHandled;
    private int themeMode = THEME_SYSTEM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeMode = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_THEME, THEME_SYSTEM);
        load();
        if (lists.isEmpty()) {
            ShoppingList first = new ShoppingList("Mercado da semana");
            first.items.add(new ShoppingItem("Arroz", 24.90));
            first.items.add(new ShoppingItem("Leite", 5.49));
            first.items.add(new ShoppingItem("Frutas", 0));
            lists.add(first);
            save();
        }
        showSplash();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            shellReady = true;
            handleIncomingIntent(getIntent());
            pendingIntentHandled = true;
            if (selectedIndex >= 0) {
                showListScreen();
            } else {
                showHomeScreen();
            }
            UpdateManager.checkForUpdates(this, false);
        }, 2000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (shellReady) {
            handleIncomingIntent(intent);
            if (selectedIndex >= 0) showListScreen(); else showHomeScreen();
        }
    }

    @Override
    public void onBackPressed() {
        if (selectedIndex >= 0) {
            selectedIndex = -1;
            showHomeScreen();
            return;
        }
        super.onBackPressed();
    }

    private void showSplash() {
        LinearLayout splash = new LinearLayout(this);
        splash.setOrientation(LinearLayout.VERTICAL);
        splash.setGravity(Gravity.CENTER);
        splash.setPadding(dp(24), dp(24), dp(24), dp(24));
        splash.setBackgroundColor(screenBg());

        ImageView image = new ImageView(this);
        image.setImageResource(getResources().getIdentifier("splash_art", "drawable", getPackageName()));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        splash.addView(image, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        TextView name = new TextView(this);
        name.setText("CompraLink");
        name.setTextColor(accent());
        name.setTextSize(34);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setGravity(Gravity.CENTER);
        splash.addView(name, matchWrapWithTop(dp(18)));

        TextView tag = new TextView(this);
        tag.setText("listas, precos e compartilhamento");
        tag.setTextColor(mutedText());
        tag.setTextSize(15);
        tag.setGravity(Gravity.CENTER);
        splash.addView(tag, matchWrapWithTop(dp(4)));

        setContentView(splash);
    }

    private void showHomeScreen() {
        selectedIndex = -1;
        buildRoot();
        addTopHeader("Suas listas", "Crie listas e compare precos salvos.", false);

        for (int i = 0; i < lists.size(); i++) {
            root.addView(listCard(i), matchWrapWithTop(dp(10)));
        }
        setContentView(rootScroll());
    }

    private void showListScreen() {
        buildRoot();
        ShoppingList list = lists.get(selectedIndex);
        addTopHeader(list.name, listSubtitle(list), true);
        addInputCard();

        addItems(false);
        addItems(true);
        setContentView(rootScroll());
    }

    private void buildRoot() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(22));
    }

    private ScrollView rootScroll() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(screenBg());
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return scrollView;
    }

    private void addTopHeader(String heading, String subheading, boolean listOpen) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(18), dp(18), dp(18));
        header.setBackground(round(cardBg(), dp(20), stroke(), 1));
        root.addView(header, matchWrap());

        TextView appName = new TextView(this);
        appName.setText("CompraLink");
        appName.setTextColor(accent());
        appName.setTextSize(14);
        appName.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(appName);

        TextView title = new TextView(this);
        title.setText(heading);
        title.setTextColor(primaryText());
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(6), 0, 0);
        header.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(subheading);
        subtitle.setTextColor(mutedText());
        subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(4), 0, dp(14));
        header.addView(subtitle);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(actions, matchWrap());

        if (listOpen) {
            Button back = button("Voltar", softButtonBg(), primaryText());
            back.setOnClickListener(v -> {
                selectedIndex = -1;
                showHomeScreen();
            });
            actions.addView(back, weighted());

            Button share = button("Compartilhar", Color.rgb(20, 184, 166), Color.WHITE);
            share.setOnClickListener(v -> shareSelectedList());
            LinearLayout.LayoutParams shareParams = weighted();
            shareParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(share, shareParams);

            Button theme = iconButton(themeIcon(), isDarkTheme() ? Color.WHITE : Color.BLACK, isDarkTheme() ? Color.BLACK : Color.WHITE);
            theme.setOnClickListener(v -> toggleTheme());
            LinearLayout.LayoutParams themeParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            themeParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(theme, themeParams);
        } else {
            Button newList = button("+ Lista", Color.rgb(15, 118, 110), Color.WHITE);
            newList.setOnClickListener(v -> promptNewList());
            actions.addView(newList, weighted());

            Button update = button("Atualizar", isDarkTheme() ? Color.rgb(71, 85, 105) : Color.rgb(51, 65, 85), Color.WHITE);
            update.setOnClickListener(v -> UpdateManager.checkForUpdates(this, true));
            LinearLayout.LayoutParams updateParams = weighted();
            updateParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(update, updateParams);

            Button theme = iconButton(themeIcon(), isDarkTheme() ? Color.WHITE : Color.BLACK, isDarkTheme() ? Color.BLACK : Color.WHITE);
            theme.setOnClickListener(v -> toggleTheme());
            LinearLayout.LayoutParams themeParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            themeParams.setMargins(dp(8), 0, 0, 0);
            actions.addView(theme, themeParams);
        }
    }

    private View listCard(int index) {
        ShoppingList list = lists.get(index);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackground(round(cardBg(), dp(16), stroke(), 1));
        card.setOnClickListener(v -> {
            selectedIndex = index;
            showListScreen();
        });
        card.setOnLongClickListener(v -> {
            confirmDeleteList(index);
            return true;
        });

        TextView name = new TextView(this);
        name.setText(list.name);
        name.setTextSize(19);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(primaryText());
        card.addView(name);

        TextView meta = new TextView(this);
        meta.setText(listSubtitle(list));
        meta.setTextSize(14);
        meta.setTextColor(mutedText());
        meta.setPadding(0, dp(5), 0, 0);
        card.addView(meta);
        return card;
    }

    private String listSubtitle(ShoppingList list) {
        int done = 0;
        double total = 0;
        for (ShoppingItem item : list.items) {
            if (item.checked) done++;
            if (item.price > 0) total += item.price;
        }
        return list.items.size() + " itens, " + done + " concluidos, total " + money.format(total);
    }

    private void addInputCard() {
        LinearLayout addCard = new LinearLayout(this);
        boolean compact = isCompactWidth();
        addCard.setOrientation(compact ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        addCard.setGravity(Gravity.CENTER_VERTICAL);
        addCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        addCard.setBackground(round(cardBg(), dp(18), stroke(), 1));
        root.addView(addCard, matchWrapWithTop(dp(12)));

        itemInput = new EditText(this);
        itemInput.setSingleLine(true);
        itemInput.setHint("Produto");
        itemInput.setTextColor(primaryText());
        itemInput.setHintTextColor(mutedText());
        itemInput.setTextSize(16);
        itemInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        itemInput.setBackground(round(inputBg(), dp(14), stroke(), 1));
        itemInput.setPadding(dp(12), 0, dp(12), 0);
        addCard.addView(itemInput, compact ? matchHeight(dp(54)) : weightedHeight(dp(54)));

        priceInput = new EditText(this);
        priceInput.setSingleLine(true);
        priceInput.setHint("Preco");
        priceInput.setGravity(Gravity.CENTER_VERTICAL);
        priceInput.setTextColor(primaryText());
        priceInput.setHintTextColor(mutedText());
        priceInput.setTextSize(15);
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceInput.setBackground(round(inputBg(), dp(14), stroke(), 1));
        priceInput.setPadding(dp(12), 0, dp(12), 0);

        Button add = button("+", Color.rgb(250, 204, 21), Color.rgb(24, 24, 27));
        add.setTextSize(22);
        add.setOnClickListener(v -> addItem());

        if (compact) {
            LinearLayout bottom = new LinearLayout(this);
            bottom.setOrientation(LinearLayout.HORIZONTAL);
            bottom.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams bottomParams = matchWrapWithTop(dp(8));
            addCard.addView(bottom, bottomParams);
            bottom.addView(priceInput, weightedHeight(dp(54)));
            LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(56), dp(54));
            addParams.setMargins(dp(8), 0, 0, 0);
            bottom.addView(add, addParams);
        } else {
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(dp(110), dp(54));
            priceParams.setMargins(dp(8), 0, dp(8), 0);
            addCard.addView(priceInput, priceParams);
            addCard.addView(add, new LinearLayout.LayoutParams(dp(56), dp(54)));
        }
    }

    private void addItems(boolean checked) {
        ShoppingList current = lists.get(selectedIndex);
        for (int i = 0; i < current.items.size(); i++) {
            ShoppingItem item = current.items.get(i);
            if (item.checked != checked) continue;
            root.addView(itemRow(item, i), matchWrapWithTop(dp(8)));
        }
    }

    private View itemRow(ShoppingItem item, int index) {
        String priceText = item.price > 0 ? money.format(item.price) : "R$ --";
        boolean priceBelow = isCompactWidth() || item.price >= 10 || priceText.length() > 7;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(9), dp(10), dp(9));
        row.setBackground(round(item.checked ? checkedBg() : cardBg(), dp(16), stroke(), 1));

        CheckBox box = new CheckBox(this);
        box.setChecked(item.checked);
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.checked = isChecked;
            save();
            showListScreen();
        });
        row.addView(box, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextSize(16);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        boolean hasHistory = hasComparablePrices(item);
        name.setTextColor(item.checked ? disabledText() :
                (hasHistory ? Color.rgb(37, 99, 235) : primaryText()));
        name.setPaintFlags(item.checked
                ? name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : name.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        name.setOnClickListener(v -> showPriceComparison(item));

        LinearLayout itemText = new LinearLayout(this);
        itemText.setOrientation(LinearLayout.VERTICAL);
        itemText.setGravity(Gravity.CENTER_VERTICAL);
        itemText.addView(name, matchWrap());

        TextView price = new TextView(this);
        price.setText(priceText);
        price.setGravity(priceBelow ? Gravity.START : Gravity.CENTER);
        price.setTextSize(14);
        price.setTypeface(Typeface.DEFAULT_BOLD);
        price.setSingleLine(false);
        price.setTextColor(item.checked ? disabledText() : accent());
        price.setOnClickListener(v -> promptPrice(item));
        if (priceBelow) {
            itemText.addView(price, matchWrapWithTop(dp(3)));
            row.addView(itemText, weighted());
        } else {
            row.addView(itemText, weighted());
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(dp(94), ViewGroup.LayoutParams.WRAP_CONTENT);
            priceParams.setMargins(dp(6), 0, dp(6), 0);
            row.addView(price, priceParams);
        }

        Button remove = button("x", Color.rgb(254, 226, 226), Color.rgb(153, 27, 27));
        remove.setTextSize(18);
        remove.setOnClickListener(v -> {
            lists.get(selectedIndex).items.remove(index);
            save();
            showListScreen();
        });
        row.addView(remove, new LinearLayout.LayoutParams(dp(42), dp(42)));
        return row;
    }

    private void addItem() {
        String text = itemInput.getText().toString().trim();
        if (text.isEmpty()) return;
        lists.get(selectedIndex).items.add(new ShoppingItem(text, parsePrice(priceInput.getText().toString())));
        save();
        hideKeyboard();
        showListScreen();
    }

    private void promptPrice(ShoppingItem item) {
        EditText input = new EditText(this);
        input.setHint("Preco");
        input.setSingleLine(true);
        input.setTextColor(primaryText());
        input.setHintTextColor(mutedText());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (item.price > 0) {
            input.setText(String.format(Locale.US, "%.2f", item.price));
            input.setSelection(input.getText().length());
        }
        new AlertDialog.Builder(this)
                .setTitle("Preco de " + item.name)
                .setView(input)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    item.price = parsePrice(input.getText().toString());
                    save();
                    showListScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showPriceComparison(ShoppingItem item) {
        List<PriceHit> hits = findComparablePrices(item);
        if (hits.isEmpty()) {
            Toast.makeText(this, "Sem historico para este produto.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(10), dp(18), dp(10));

        for (PriceHit hit : hits) {
            double saved = item.price > 0 ? item.price - hit.price : 0;
            String text = money.format(hit.price) + " - " + hit.listName;
            if (saved > 0) text += "  economiza " + money.format(saved);
            SpannableString span = new SpannableString(text);
            int priceEnd = money.format(hit.price).length();
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, priceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.rgb(15, 118, 110)), 0, priceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (saved > 0) {
                int start = text.indexOf("economiza");
                span.setSpan(new StyleSpan(Typeface.BOLD), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new ForegroundColorSpan(Color.rgb(22, 163, 74)), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextView line = new TextView(this);
            line.setText(span);
            line.setTextSize(16);
            line.setTextColor(primaryText());
            line.setPadding(0, dp(8), 0, dp(8));
            content.addView(line, matchWrap());
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(320)));

        new AlertDialog.Builder(this)
                .setTitle("Precos mais baratos: " + item.name)
                .setView(scroll)
                .setPositiveButton("Fechar", null)
                .show();
    }

    private boolean hasComparablePrices(ShoppingItem item) {
        return !findComparablePrices(item).isEmpty();
    }

    private List<PriceHit> findComparablePrices(ShoppingItem item) {
        String target = normalize(item.name);
        List<PriceHit> hits = new ArrayList<>();
        for (ShoppingList list : lists) {
            for (ShoppingItem other : list.items) {
                if (other == item || other.price <= 0) continue;
                if (!normalize(other.name).equals(target)) continue;
                if (item.price > 0 && other.price >= item.price) continue;
                hits.add(new PriceHit(list.name, other.price));
            }
        }
        Collections.sort(hits, Comparator.comparingDouble(hit -> hit.price));
        return hits;
    }

    private void promptNewList() {
        EditText input = new EditText(this);
        input.setHint("Nome da lista");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        new AlertDialog.Builder(this)
                .setTitle("Nova lista")
                .setView(input)
                .setPositiveButton("Criar", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "Nova lista";
                    lists.add(new ShoppingList(name));
                    save();
                    showHomeScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmDeleteList(int index) {
        if (lists.size() == 1) {
            Toast.makeText(this, "Mantenha pelo menos uma lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Remover lista?")
                .setMessage(lists.get(index).name)
                .setPositiveButton("Remover", (dialog, which) -> {
                    lists.remove(index);
                    save();
                    showHomeScreen();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void shareSelectedList() {
        try {
            String link = buildShareLink(lists.get(selectedIndex));
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, "Lista de compras: " + lists.get(selectedIndex).name);
            send.putExtra(Intent.EXTRA_TEXT, "Lista de compras CompraLink:\n" + link);
            startActivity(Intent.createChooser(send, "Compartilhar lista"));

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("CompraLink", link));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Nao foi possivel compartilhar esta lista.", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildShareLink(ShoppingList list) throws Exception {
        String payload = encodeCompressed(list.toJson().toString());
        return SHARE_BASE + payload;
    }

    private void handleIncomingIntent(Intent intent) {
        if (pendingIntentHandled && intent == getIntent()) return;
        if (intent == null) return;
        Uri data = intent.getData();
        if (data != null) {
            String payload = null;
            if (("http".equals(data.getScheme()) || "https".equals(data.getScheme()))
                    && "compralink.app".equals(data.getHost())) {
                if (data.getPath() != null && data.getPath().startsWith("/l/")) {
                    payload = data.getLastPathSegment();
                } else if ("/list".equals(data.getPath())) {
                    payload = data.getQueryParameter("payload");
                }
            } else if ("compralink".equals(data.getScheme()) && "list".equals(data.getHost())) {
                payload = data.getQueryParameter("payload");
            }
            if (payload != null) importPayload(payload);
        }
    }

    private void importPayload(String rawPayload) {
        if (rawPayload == null || rawPayload.trim().isEmpty()) return;
        String payload = rawPayload.trim();
        int marker = payload.indexOf("payload=");
        if (marker >= 0) payload = payload.substring(marker + "payload=".length());
        int end = payload.indexOf('\n');
        if (end >= 0) payload = payload.substring(0, end);
        int space = payload.indexOf(' ');
        if (space >= 0) payload = payload.substring(0, space);
        try {
            String json = decodeCompressed(payload);
            ShoppingList imported = ShoppingList.fromJson(new JSONObject(json));
            imported.id = UUID.randomUUID().toString();
            imported.name = imported.name + " compartilhada";
            lists.add(imported);
            selectedIndex = lists.size() - 1;
            save();
            Toast.makeText(this, "Lista importada.", Toast.LENGTH_SHORT).show();
        } catch (Exception compressedFailed) {
            try {
                byte[] decoded = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
                ShoppingList imported = ShoppingList.fromJson(new JSONObject(new String(decoded, StandardCharsets.UTF_8)));
                imported.id = UUID.randomUUID().toString();
                imported.name = imported.name + " compartilhada";
                lists.add(imported);
                selectedIndex = lists.size() - 1;
                save();
            } catch (Exception e) {
                Toast.makeText(this, "Link de lista invalido.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeCompressed(String json) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(out)) {
            zip.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return Base64.encodeToString(out.toByteArray(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    private String decodeCompressed(String payload) throws Exception {
        byte[] packed = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InflaterInputStream unzip = new InflaterInputStream(new ByteArrayInputStream(packed))) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = unzip.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private void load() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_LISTS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            lists.clear();
            for (int i = 0; i < array.length(); i++) {
                lists.add(ShoppingList.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            lists.clear();
        }
    }

    private void save() {
        JSONArray array = new JSONArray();
        for (ShoppingList list : lists) {
            try {
                array.put(list.toJson());
            } catch (JSONException ignored) {
            }
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_LISTS, array.toString()).apply();
    }

    private double parsePrice(String raw) {
        if (raw == null) return 0;
        String cleaned = raw.trim().replace("R$", "").replace(" ", "");
        if (cleaned.isEmpty()) return 0;
        if (cleaned.contains(",") && cleaned.contains(".")) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else {
            cleaned = cleaned.replace(",", ".");
        }
        try {
            return Math.max(0, Double.parseDouble(cleaned));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String normalize(String value) {
        String noAccent = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.trim().toLowerCase(Locale.ROOT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && itemInput != null) imm.hideSoftInputFromWindow(itemInput.getWindowToken(), 0);
    }

    private void toggleTheme() {
        themeMode = isDarkTheme() ? THEME_LIGHT : THEME_DARK;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_THEME, themeMode).apply();
        if (selectedIndex >= 0) showListScreen(); else showHomeScreen();
    }

    private String themeIcon() {
        return isDarkTheme() ? "☀" : "☾";
    }

    private boolean isCompactWidth() {
        return getResources().getConfiguration().screenWidthDp < 390;
    }

    private boolean isDarkTheme() {
        if (themeMode == THEME_DARK) return true;
        if (themeMode == THEME_LIGHT) return false;
        int mask = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mask == Configuration.UI_MODE_NIGHT_YES;
    }

    private int screenBg() {
        return isDarkTheme() ? Color.rgb(2, 6, 23) : Color.rgb(248, 250, 252);
    }

    private int cardBg() {
        return isDarkTheme() ? Color.rgb(15, 23, 42) : Color.WHITE;
    }

    private int checkedBg() {
        return isDarkTheme() ? Color.rgb(30, 41, 59) : Color.rgb(241, 245, 249);
    }

    private int inputBg() {
        return isDarkTheme() ? Color.rgb(30, 41, 59) : Color.rgb(248, 250, 252);
    }

    private int softButtonBg() {
        return isDarkTheme() ? Color.rgb(51, 65, 85) : Color.rgb(226, 232, 240);
    }

    private int stroke() {
        return isDarkTheme() ? Color.rgb(51, 65, 85) : Color.rgb(226, 232, 240);
    }

    private int primaryText() {
        return isDarkTheme() ? Color.rgb(241, 245, 249) : Color.rgb(15, 23, 42);
    }

    private int mutedText() {
        return isDarkTheme() ? Color.rgb(148, 163, 184) : Color.rgb(71, 85, 105);
    }

    private int disabledText() {
        return isDarkTheme() ? Color.rgb(100, 116, 139) : Color.rgb(148, 163, 184);
    }

    private int accent() {
        return isDarkTheme() ? Color.rgb(45, 212, 191) : Color.rgb(15, 118, 110);
    }

    private Button button(String text, int bg, int fg) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(fg);
        button.setTextSize(14);
        button.setMinHeight(dp(48));
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(round(bg, dp(14), Color.TRANSPARENT, 0));
        return button;
    }

    private Button iconButton(String text, int bg, int fg) {
        Button button = button(text, bg, fg);
        button.setTextSize(22);
        button.setMinWidth(dp(48));
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private GradientDrawable round(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int top) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, top, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams matchHeight(int height) {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    }

    private LinearLayout.LayoutParams weightedHeight(int height) {
        return new LinearLayout.LayoutParams(0, height, 1);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class PriceHit {
        final String listName;
        final double price;

        PriceHit(String listName, double price) {
            this.listName = listName;
            this.price = price;
        }
    }

    private static class ShoppingList {
        String id = UUID.randomUUID().toString();
        String name;
        final List<ShoppingItem> items = new ArrayList<>();

        ShoppingList(String name) {
            this.name = name;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            JSONArray array = new JSONArray();
            for (ShoppingItem item : items) array.put(item.toJson());
            json.put("items", array);
            return json;
        }

        static ShoppingList fromJson(JSONObject json) throws JSONException {
            ShoppingList list = new ShoppingList(json.optString("name", "Lista"));
            list.id = json.optString("id", UUID.randomUUID().toString());
            JSONArray array = json.optJSONArray("items");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    list.items.add(ShoppingItem.fromJson(array.getJSONObject(i)));
                }
            }
            return list;
        }
    }

    private static class ShoppingItem {
        String name;
        boolean checked;
        double price;

        ShoppingItem(String name, double price) {
            this.name = name;
            this.price = price;
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("checked", checked);
            json.put("price", price);
            return json;
        }

        static ShoppingItem fromJson(JSONObject json) {
            ShoppingItem item = new ShoppingItem(json.optString("name", "Item"), json.optDouble("price", 0));
            item.checked = json.optBoolean("checked", false);
            return item;
        }
    }
}
