package mikecanco.de.cupboardtest;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        db = dbHelper.getWritableDatabase();

        // here is where you associate the name array.
        //TODO: make sure name array is populated!
        bunnyNameArray = getAllBunniesNames();
        bunnyAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, bunnyNameArray);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = etBunnyName.getText().toString();
                if(!s.isEmpty()){

                    Integer cute= (int) ((long)new Random(100).nextLong());
                    Bunny b = new Bunny(s, cute);
                    cupboard().withDatabase(db).put(b);
                    bunnyAdapter.add(b.getName());
                    bunnyAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(getApplicationContext(), "no empty bunnies", Toast.LENGTH_SHORT).show();
                }

            }
        });

        lvBunnies.setAdapter(bunnyAdapter);
        lvBunnies.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {

                String name = (String) lvBunnies.getItemAtPosition(pos);
                cupboard().withDatabase(db).delete(Bunny.class, "name = ?", name);
                bunnyNameArray.remove(pos);
                bunnyAdapter.notifyDataSetChanged();

                return false;
            }
        });

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

    public static ArrayList<String> getAllBunniesNames() {
        final QueryResultIterable<Bunny> iter = cupboard().withDatabase(db).query(Bunny.class).query();
        List<Bunny> bunnies = getListFromQueryResultIterator(iter);
        ArrayList<String> bunnyNameArray = new ArrayList<String>();
        for (Bunny b: bunnies){
            bunnyNameArray.add(b.getName());
        }
        return bunnyNameArray;
    }


//    private static DatabaseCompartment.QueryBuilder<Bunny> getQueryBuilder() {
//        return cupboard().withDatabase(db).query(Bunny.class);
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}
