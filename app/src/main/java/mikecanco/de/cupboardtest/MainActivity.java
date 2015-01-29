package mikecanco.de.cupboardtest;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nl.qbusict.cupboard.QueryResultIterable;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class MainActivity extends Activity {

    static SQLiteDatabase db;

    EditText etBunnyName;
    Button btnAdd;
    ListView lvBunnies;

    ArrayAdapter<String> bunnyAdapter;
    ArrayList<Bunny> bunnyArray;
    ArrayList<String> bunnyNameArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        etBunnyName = (EditText) findViewById(R.id.edit_text);
        btnAdd = (Button) findViewById(R.id.button2);
        lvBunnies = (ListView) findViewById(R.id.listView);

        // setup database
        PracticeDatabaseHelper dbHelper = new PracticeDatabaseHelper(this);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 2);
        db = dbHelper.getWritableDatabase();

        // here is where you associate the name array.
        bunnyNameArray = getAllBunniesNames();

        bunnyAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, android.R.id.text1, bunnyNameArray) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText("This bunny is named: "+bunnyArray.get(position).getName());
                text2.setText("It is a "+bunnyArray.get(position).getCutenessTypeEnum()+" bunny");
                return view;
            }
        };

        // clicking this button adds a new bunny with your chosen name to the database
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = etBunnyName.getText().toString();
                if (!s.isEmpty()) {
                    Bunny b = new Bunny(s);
                    cupboard().withDatabase(db).put(b);
                    bunnyArray.add(b);
                    bunnyAdapter.add(b.getName());
                    bunnyAdapter.notifyDataSetChanged();
                    // empty the edit text
                    etBunnyName.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "no empty bunnies", Toast.LENGTH_SHORT).show();
                }

            }
        });

        lvBunnies.setAdapter(bunnyAdapter);

        // long clicking on a list item will remove the bunny from the list AND from the db
        lvBunnies.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {

                Bunny b = bunnyArray.get(pos);
                cupboard().withDatabase(db).delete(Bunny.class, b.get_id());
                bunnyArray.remove(pos);
                bunnyNameArray.remove(pos);
                bunnyAdapter.notifyDataSetChanged();

                return false;
            }
        });

        //clicking on the bunny updates their cuteness value to VERYCUTE and persists that change
        lvBunnies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                // as an example of updating a value, and persisting it back to the db
                Bunny b = bunnyArray.get(pos);
                b.setCuteValue(85);
                b.setCutenessTypeEnum(Bunny.cutenessType.VERYCUTE);
                cupboard().withDatabase(db).put(b);
                bunnyAdapter.notifyDataSetChanged();
            }
        });

        getListOfVeryCuteBunniesNamedSteve();

    }


     /* Private Methods */

    private static List<Bunny> getListFromQueryResultIterator(QueryResultIterable<Bunny> iter) {

        final List<Bunny> bunnies = new ArrayList<Bunny>();
        for (Bunny bunny : iter) {
            bunnies.add(bunny);
        }
        iter.close();

        return bunnies;
    }

    public ArrayList<String> getAllBunniesNames() {
        final QueryResultIterable<Bunny> iter = cupboard().withDatabase(db).query(Bunny.class).query();
        bunnyArray = (ArrayList<Bunny>) getListFromQueryResultIterator(iter);

        ArrayList<String> bunnyNameArray = new ArrayList<String>();
        for (Bunny b : bunnyArray) {
            bunnyNameArray.add(b.getName());
        }

        return bunnyNameArray;
    }

    // this is an example method demonstrating how one would query cupboard for a specific subset
    // of objects. In this case, very cute bunnies named steve.
    public static List<Bunny> getListOfVeryCuteBunniesNamedSteve() {
        String selectionString = "name='steve' AND cuteValue > 66";
//        Long time = Calendar.getInstance().getTimeInMillis();
        QueryResultIterable<Bunny> iterable =
                cupboard().withDatabase(db).query(Bunny.class).withSelection(selectionString).query();
        List<Bunny> list = getListFromQueryResultIterator(iterable);
        return list;
    }
}