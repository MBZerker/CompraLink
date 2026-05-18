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
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String PREFS = "compralink";
    private static final String KEY_LISTS = "lists";
    private static final String HTTPS_SHARE_PREFIX = "https://compralink.app/list?payload=";
    private static final String CUSTOM_SHARE_PREFIX = "compralink://list?payload=";

    private final List<ShoppingList> lists = new ArrayList<>();
    private LinearLayout root;
    private LinearLayout tabs;
    private LinearLayout itemList;
    private TextView title;
    private TextView subtitle;
    private EditText itemInput;
    private EditText priceInput;
    private int selectedIndex = 0;
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        load();
        if (lists.isEmpty()) {
            ShoppingList first = new ShoppingList("Mercado da semana");
            first.items.add(new ShoppingItem("Arroz", 24.90));
            first.items.add(new ShoppingItem("Leite", 5.49));
            first.items.add(new ShoppingItem("Frutas", 0));
            lists.add(first);
            save();
        }

        buildShell();
        handleIncomingIntent(getIntent());
        render();
        UpdateManager.checkForUpdates(this, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
        render();
    }

    private void buildShell() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(248, 250, 252));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(22));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(18), dp(18), dp(18));
        header.setBackground(round(Color.WHITE, dp(22), Color.rgb(226, 232, 240), 1));
        root.addView(header, matchWrap());

        TextView appName = new TextView(this);
        appName.setText("CompraLink");
        appName.setTextColor(Color.rgb(15, 118, 110));
        appName.setTextSize(14);
        appName.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(appName);

        title = new TextView(this);
        title.setTextColor(Color.rgb(15, 23, 42));
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setPadding(0, dp(6), 0, 0);
        header.addView(title);

        subtitle = new TextView(this);
        subtitle.setTextColor(Color.rgb(71, 85, 105));
        subtitle.setTextSize(14);
        subtitle.setPadding(0, dp(4), 0, dp(14));
        header.addView(subtitle);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(actions, matchWrap());

        Button newList = button("+ Lista", Color.rgb(15, 118, 110), Color.WHITE);
        newList.setOnClickListener(v -> promptNewList());
        actions.addView(newList, weighted());

        Button share = button("Compartilhar", Color.rgb(20, 184, 166), Color.WHITE);
        share.setOnClickListener(v -> shareSelectedList());
        LinearLayout.LayoutParams shareParams = weighted();
        shareParams.setMargins(dp(10), 0, 0, 0);
        actions.addView(share, shareParams);

        Button update = button("Atualizar", Color.rgb(51, 65, 85), Color.WHITE);
        update.setOnClickListener(v -> UpdateManager.checkForUpdates(this, true));
        LinearLayout.LayoutParams updateParams = weighted();
        updateParams.setMargins(dp(10), 0, 0, 0);
        actions.addView(update, updateParams);

        HorizontalScrollView tabScroll = new HorizontalScrollView(this);
        tabScroll.setHorizontalScrollBarEnabled(false);
        tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, dp(16), 0, dp(8));
        tabScroll.addView(tabs);
        root.addView(tabScroll, matchWrap());

        LinearLayout addCard = new LinearLayout(this);
        addCard.setOrientation(LinearLayout.HORIZONTAL);
        addCard.setGravity(Gravity.CENTER_VERTICAL);
        addCard.setPadding(dp(12), dp(10), dp(12), dp(10));
        addCard.setBackground(round(Color.WHITE, dp(18), Color.rgb(226, 232, 240), 1));
        root.addView(addCard, matchWrapWithTop(dp(6)));

        itemInput = new EditText(this);
        itemInput.setSingleLine(true);
        itemInput.setHint("Produto");
        itemInput.setTextSize(16);
        itemInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        itemInput.setBackgroundColor(Color.TRANSPARENT);
        addCard.addView(itemInput, weighted());

        priceInput = new EditText(this);
        priceInput.setSingleLine(true);
        priceInput.setHint("R$");
        priceInput.setGravity(Gravity.CENTER);
        priceInput.setTextSize(15);
        priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceInput.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(dp(76), ViewGroup.LayoutParams.WRAP_CONTENT);
        priceParams.setMargins(dp(8), 0, dp(8), 0);
        addCard.addView(priceInput, priceParams);

        Button add = button("+", Color.rgb(250, 204, 21), Color.rgb(24, 24, 27));
        add.setTextSize(22);
        add.setOnClickListener(v -> addItem());
        addCard.addView(add, new LinearLayout.LayoutParams(dp(52), dp(48)));

        itemList = new LinearLayout(this);
        itemList.setOrientation(LinearLayout.VERTICAL);
        root.addView(itemList, matchWrapWithTop(dp(12)));

        setContentView(scrollView);
    }

    private void render() {
        if (selectedIndex >= lists.size()) {
            selectedIndex = Math.max(0, lists.size() - 1);
        }
        ShoppingList current = lists.get(selectedIndex);
        title.setText(current.name);

        int done = 0;
        double total = 0;
        for (ShoppingItem item : current.items) {
            if (item.checked) done++;
            if (item.price > 0) total += item.price;
        }
        subtitle.setText(current.items.size() + " itens, " + done + " concluidos, total " + money.format(total));

        tabs.removeAllViews();
        for (int i = 0; i < lists.size(); i++) {
            final int index = i;
            TextView chip = new TextView(this);
            chip.setText(lists.get(i).name);
            chip.setTextSize(14);
            chip.setGravity(Gravity.CENTER);
            chip.setTypeface(Typeface.DEFAULT_BOLD);
            chip.setSingleLine(true);
            chip.setPadding(dp(15), dp(10), dp(15), dp(10));
            chip.setTextColor(i == selectedIndex ? Color.WHITE : Color.rgb(51, 65, 85));
            chip.setBackground(round(i == selectedIndex ? Color.rgb(15, 118, 110) : Color.WHITE,
                    dp(18), Color.rgb(226, 232, 240), 1));
            chip.setOnClickListener(v -> {
                selectedIndex = index;
                render();
            });
            chip.setOnLongClickListener(v -> {
                confirmDeleteList(index);
                return true;
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dp(42)
            );
            params.setMargins(0, 0, dp(8), 0);
            tabs.addView(chip, params);
        }

        itemList.removeAllViews();
        addItems(false);
        addItems(true);
    }

    private void addItems(boolean checked) {
        ShoppingList current = lists.get(selectedIndex);
        for (int i = 0; i < current.items.size(); i++) {
            ShoppingItem item = current.items.get(i);
            if (item.checked != checked) continue;
            itemList.addView(itemRow(item, i), matchWrapWithTop(dp(8)));
        }
    }

    private View itemRow(ShoppingItem item, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(10), dp(10));
        row.setBackground(round(item.checked ? Color.rgb(241, 245, 249) : Color.WHITE,
                dp(16), Color.rgb(226, 232, 240), 1));

        CheckBox box = new CheckBox(this);
        box.setChecked(item.checked);
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.checked = isChecked;
            save();
            render();
        });
        row.addView(box, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextSize(17);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        boolean hasHistory = hasComparablePrices(item);
        name.setTextColor(item.checked ? Color.rgb(148, 163, 184) :
                (hasHistory ? Color.rgb(37, 99, 235) : Color.rgb(15, 23, 42)));
        name.setPaintFlags(item.checked
                ? name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : name.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        name.setOnClickListener(v -> showPriceComparison(item));
        row.addView(name, weighted());

        TextView price = new TextView(this);
        price.setText(item.price > 0 ? money.format(item.price) : "R$ --");
        price.setGravity(Gravity.CENTER);
        price.setTextSize(15);
        price.setTypeface(Typeface.DEFAULT_BOLD);
        price.setTextColor(item.checked ? Color.rgb(148, 163, 184) : Color.rgb(15, 118, 110));
        price.setOnClickListener(v -> promptPrice(item));
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(dp(88), dp(44));
        priceParams.setMargins(dp(8), 0, dp(8), 0);
        row.addView(price, priceParams);

        Button remove = button("x", Color.rgb(254, 226, 226), Color.rgb(153, 27, 27));
        remove.setTextSize(20);
        remove.setOnClickListener(v -> {
            lists.get(selectedIndex).items.remove(index);
            save();
            render();
        });
        row.addView(remove, new LinearLayout.LayoutParams(dp(44), dp(44)));
        return row;
    }

    private void addItem() {
        String text = itemInput.getText().toString().trim();
        if (text.isEmpty()) return;
        lists.get(selectedIndex).items.add(new ShoppingItem(text, parsePrice(priceInput.getText().toString())));
        itemInput.setText("");
        priceInput.setText("");
        save();
        render();
        hideKeyboard();
    }

    private void promptPrice(ShoppingItem item) {
        EditText input = new EditText(this);
        input.setHint("Preco");
        input.setSingleLine(true);
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
                    render();
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
            TextView line = new TextView(this);
            String suffix = item.price > 0 && hit.price < item.price
                    ? "  economiza " + money.format(item.price - hit.price)
                    : "";
            line.setText(money.format(hit.price) + " - " + hit.listName + suffix);
            line.setTextSize(16);
            line.setTextColor(Color.rgb(15, 23, 42));
            line.setPadding(0, dp(8), 0, dp(8));
            content.addView(line, matchWrap());
        }

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(320)
        ));

        String titleText = item.price > 0
                ? "Precos mais baratos"
                : "Precos salvos";
        new AlertDialog.Builder(this)
                .setTitle(titleText + ": " + item.name)
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
        for (int listIndex = 0; listIndex < lists.size(); listIndex++) {
            ShoppingList list = lists.get(listIndex);
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
                    selectedIndex = lists.size() - 1;
                    save();
                    render();
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
                    selectedIndex = Math.max(0, selectedIndex - 1);
                    save();
                    render();
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
        } catch (JSONException e) {
            Toast.makeText(this, "Nao foi possivel compartilhar esta lista.", Toast.LENGTH_SHORT).show();
        }
    }

    private String buildShareLink(ShoppingList list) throws JSONException {
        JSONObject json = list.toJson();
        String payload = Base64.encodeToString(
                json.toString().getBytes(StandardCharsets.UTF_8),
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING
        );
        return HTTPS_SHARE_PREFIX + payload;
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        Uri data = intent.getData();
        if (data != null) {
            if ("compralink".equals(data.getScheme()) && "list".equals(data.getHost())) {
                importPayload(data.getQueryParameter("payload"));
                return;
            }
            if (("http".equals(data.getScheme()) || "https".equals(data.getScheme()))
                    && "compralink.app".equals(data.getHost())
                    && "/list".equals(data.getPath())) {
                importPayload(data.getQueryParameter("payload"));
                return;
            }
        }
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (text != null) {
                int start = text.indexOf(HTTPS_SHARE_PREFIX);
                if (start < 0) start = text.indexOf(CUSTOM_SHARE_PREFIX);
                if (start >= 0) {
                    importPayload(text.substring(start).trim());
                }
            }
        }
    }

    private void importPayload(String rawPayload) {
        if (rawPayload == null || rawPayload.trim().isEmpty()) return;
        String payload = rawPayload.trim();
        int marker = payload.indexOf("payload=");
        if (marker >= 0) {
            payload = payload.substring(marker + "payload=".length());
        }
        int end = payload.indexOf('\n');
        if (end >= 0) payload = payload.substring(0, end);
        int space = payload.indexOf(' ');
        if (space >= 0) payload = payload.substring(0, space);
        try {
            byte[] decoded = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            ShoppingList imported = ShoppingList.fromJson(new JSONObject(new String(decoded, StandardCharsets.UTF_8)));
            imported.id = UUID.randomUUID().toString();
            imported.name = imported.name + " compartilhada";
            lists.add(imported);
            selectedIndex = lists.size() - 1;
            save();
            Toast.makeText(this, "Lista importada.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Link de lista invalido.", Toast.LENGTH_SHORT).show();
        }
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
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_LISTS, array.toString())
                .apply();
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
        if (imm != null) imm.hideSoftInputFromWindow(itemInput.getWindowToken(), 0);
    }

    private Button button(String text, int bg, int fg) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(fg);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackground(round(bg, dp(14), Color.TRANSPARENT, 0));
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
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int top) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, top, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
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
            for (ShoppingItem item : items) {
                array.put(item.toJson());
            }
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
