# cupboardTestApp
A test app I have written to demonstrate how to use the cupboard SQLite helper for android

Cupboard guide for CodePath
## Overview

Cupboard is a way to manage persistence in a sqlite instance for your app. It's a small library, simple to use, and it was designed specifically for Android unlike ORMlite.

Using the Cupboard persistence library makes managing client-side models extremely easy in simple cases. For more advanced or custom cases, you can use [[SQLiteOpenHelper|Local-Databases-with-SQLiteOpenHelper]] to manage the database communication directly. However, keep in mind that Cupboard was written with the intention to abstract away a lot of boilerplate and reused code that would go into making SQLiteOpenHelper function. 

<img src="include some picture here" width="500" alt="orm" />

<!-- Note that while this library does not enforce the DOA model, we are going to leverage it regardless as it adds to overall code clarity.  -->

<!-- 
Cupboard works like an **Object Relational Mapper** by mapping java classes to database tables and mapping java class member variables to the table columns. Through this process, **each table** maps to a **Java model** and **the columns** in the table represent the respective **data fields**. Similarly, each row in the database represents a particular object. This allows us to create, modify, delete and query our SQLite database using model objects instead of raw SQL.

For example, a "Tweet" model would be mapped to a "tweets" table in the database. The Tweet model might have a "body" field that maps to a body column in the table and a "timestamp" field that maps to a timestamp column. Through this process, each row would map to a particular tweet. -->

### Installation

To install manually, you can [download the latest JAR file](https://search.maven.org/remote_content?g=nl.qbusict&a=cupboard&v=LATEST)

To install Cupboard with Maven, simply add the line

```xml
<dependency>
    <groupId>nl.qbusict</groupId>
    <artifactId>cupboard</artifactId>
    <version>(insert latest version)</version>
</dependency>
```

To install Cupboard with Gradle, simply add the line

```groovy
compile 'nl.qbusict:cupboard:(insert latest version)'
```

to the dependencies section of your app's build.gradle file.

You should now have ahold of the files you need for Cupboard.


### Configuration

Next, we'll setup a custom SQLiteOpenHelper. This is a standard object in the Android framework that assists in dealing with SQLite databases. For now, we'll just create the object and register one Plain Old Java Object (POJO) in our database: `Bunny`
```java
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class PracticeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cupboardTest.db";
    private static final int DATABASE_VERSION = 1;

    public PracticeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static {
        // register our models
        cupboard().register(Bunny.class);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        cupboard().withDatabase(db).createTables();
        // add indexes and other database tweaks in this method if you want

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work if you have an alteration to make to your schema here

    }

}

```

After this, somewhere in your app where you want to use Cupboard, you will have to instantiate your DatabaseHelper. 
```java
PracticeDatabaseHelper dbHelper = new PracticeDatabaseHelper(this);
db = dbHelper.getWritableDatabase();
```

Now, with your database instantiated, you are ready to use Cupboard.

### Usage

#### Defining Models

First, we define our models by creating POJO's to represent them. However, a POJO for cupboard must always contain a variable of type `Long` called `_id`. This will serve as the index for the pojo within the SQLite table. 

```java
public class Bunny {

    public Long _id; // for cupboard
    public String name; // bunny name
    public Integer cuteValue ; // bunny cuteness


    public Bunny() {
        this.name = "noName";
        this.cuteValue = 0;
    }

    public Bunny(String name, Integer cuteValue) {
        this.name = name;
        this.cuteValue = cuteValue;
    }
}
```

The name part of the class will be the name of the Table and the names of the variables will be those of the Columns, so make sure to use the SQLite naming conventions for those.

Then, as seen above in setting up the database, you must register the class in your DatabaseHelper's static constructor. 

```java
    static {
        // register our models
        cupboard().register(Bunny.class);
    }
```

#### CRUD Operations

(this section needs to be longer for cupboard)

Now we can create, modify and delete records for these models backed by SQLite:

#####Create items
```java
// Create a Bunny
Bunny bunny = ...
long id = cupboard().withDatabase(db).put(bunny);
```
If Bunny has it's _id field set, then any existing Bunny with that id will be replaced. If _id is null then a new Bunny will be created in the table. In both cases the corresponding id is returned from put().

#####Read items

We can query records with a simple query syntax `get` method.

```java
Bunny bunny = cupboard().withDatabase(db).get(Bunny.class, 12L);
```

This will return the bunny with _id = 12. If no such bunny exists, then it will return null. 

We can also use the `get` command.
```java
// get the first bunny in the result
Bunny bunny = cupboard().withDatabase(db).query(Bunny.class).get();
// Get the cursor for this query
Cursor bunnies = cupboard().withDatabase(db).query(Bunny.class).getCursor();
try {
  // Iterate Bunnys
  QueryResultIterable<Bunny> itr = cupboard().withDatabase(db).query(Bunny.class).iterate();
  for (Bunny bunny : itr) {
    // do something with bunny
  }
} finally {
  // close the cursor
  itr.close();
}
// Get the first matching Bunny with name Max
Bunny bunny = cupboard().withDatabase(db).query(Bunny.class).withSelection( "name = ?", "Max").get();
```

#####Update items

To update an item, simply retrieve it from the database, update it's accessible fields, and use `put()` to get it back into the database

```java
cupboard().withDatabase(db).put(b);
```

However, if you're doing a larger update of items, you can use the `update()` command. This is done with a [ContentValues](http://developer.android.com/reference/android/content/ContentValues.html) object.

```java
//Let's consider the problem in which we've forgotten to capitalize the 'M' in all bunnies named 'Max'
ContentValues values = new ContentValues(1);
values.put("name", "Max")
// update all books where the title is 'android'
cupboard().withDatabase(db).update(Book.class, values, "name = ?", "max");
```

#####Delete items
```java
// Delete an item, in this case the item where _id = 12
cupboard().withDatabase(db).delete(Bunny.class, 12L);

// by passing in the item
cupboard().withDatabase(db).delete(bunny);
// or by passing in a query. This will delete all bunnies named "Max"
cupboard().withDatabase(db).delete(Bunny.class, "name = ?", "Max");
```

That's Cupboard in a nutshell. 


                            #### Migrations

If you need to add a field to your an existing model, you'll need to write a migration to add the column to the table that represents your model. Here's how:

1. Add a new field to your existing model:
  ```java
  
  ```

2. Change the database version the the AndroidManifest.xml's metadata. Increment by 1 from the last version:

  ```java

  ```

3. Write your migration script. Name your script [newDatabaseVersion].sql, and place it in the directory [YourApp’sName]/app/src/main/assets/migrations. In my specific example, I’ll create the file [MyAppName]/app/src/main/assets/migrations/2.sql. (You might have to create the migrations directory yourself). You should write the SQLite script to add a column here:

  ```sql
  ALTER TABLE Items ADD COLUMN Priority TEXT;
  ```

Note that in order trigger the migration script, you’ll have to save an instance of your model somewhere in your code. 

#### Populating ListView with CursorAdapter

Review this [[Custom CursorAdapter and ListViews|Populating a ListView with a CursorAdapter]] guide in order to load content from a `Cursor` into a `ListView`. In summary, in order to populate a `ListView` directly from the content within the Cupboard SQLite database, we can define this method on the model to retrieve a `Cursor` for the result set:

```java
public class TodoItem extends Model {
    // ...

    // Return cursor for result set for all todo items
    public static Cursor fetchResultCursor() {
        String tableName = Cache.getTableInfo(TodoItem.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
            from(TodoItem.class).toSql();
        // Execute query on the underlying Cupboard SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }
}
```

We need to define a custom `TodoCursorAdapter` as [[outlined here|Populating-a-ListView-with-a-CursorAdapter#defining-the-adapter]] in order to define which XML template to use for the cursor rows and how to populate the template views with the relevant data. 

Next, we can fetch the data cursor containing all todo items with `TodoItem.fetchResultCursor()` and populate the `ListView` using our custom `CursorAdapter`:

```java
// Find ListView to populate

// Get data cursor

// Setup cursor adapter

// Attach cursor adapter to ListView 

```

That's all we have to do to load data from Cupboard directly through a `Cursor` into a list.

                            #### Loading with Content Providers

Instead of using the underlying SQLite database directly, we can instead expose the Cupboard data as a content provider with a few simple additions. First, override the default identity column for all Cupboard models:

```java
@Table(name = "Items", id = BaseColumns._ID)
public class Item extends Model { ... }
```

Then you can use the [SimpleCursorAdapter](http://developer.android.com/reference/android/widget/SimpleCursorAdapter.html) to populate adapters using the underlying database directly:

```java
// Define a SimpleCursorAdapter loading the body into the TextView in simple_list_item_1
SimpleCursorAdapter adapterTodo = new SimpleCursorAdapter(getActivity(),
  android.R.layout.simple_list_item_1, null,
  new String[] { "body" },
  new int[] { android.R.id.text1 },
  0);
// Attach the simple adapter to the list
myListView.setAdapter(adapterTodo);
```

You could also use [[a custom CursorAdapter|Populating a ListView with a CursorAdapter#defining-the-adapter]] instead for more flexibility. Next, we can load the data into the list using the content provider system through a `CursorLoader`:

```java
MyActivity.this.getSupportLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle cursor) {
            return new CursorLoader(MyActivity.this,
                ContentProvider.createUri(TodoItem.class, null),
                null, null, null, null
            );
        }
        // ...
});
```

You must also register the content provider in your AndroidManifest.xml:

```xml
<application ...>
    <provider android:authorities="com.example" android:exported="false"    
              android:name="com.Cupboard.content.ContentProvider" />
    ...
</application>
```

See the full source code on the official [Cupboard ContentProviders guide](https://github.com/pardom/Cupboard/wiki/Using-the-content-provider).


Be sure to review the common questions below.

## Common Questions

> Question: How does Cupboard handle duplicate IDs? For example, I want to make sure no duplicate twitter IDs are inserted. Is there a way to specify a column is the primary key in the model?


> Question: How do you specify the data type (int, text)?  Does Cupboard automatically know what the column type should be?

The type is inferred automatically from the type of the field.

> Question: How do I store dates into Cupboard?


> Question: How do you represent a 1-1 relationship?

Cupboard is not a real ORM as it doesn't manage relations between objects, which keeps things simple.

> Question: How do I delete all the records from a table?


> Question: Is it possible to do joins with Cupboard? 


> Question: What are the best practices when interacting with the sqlite in Android, is ORM/DAO the way to go?


## References

* [Cupboard bitbucket](https://bitbucket.org/qbusict/cupboard)
* [AA Getting Started](https://github.com/pardom/Cupboard/wiki/Getting-started)
* [AA Models](https://github.com/pardom/Cupboard/wiki/Creating-your-database-model)
* [AA Saving](https://github.com/pardom/Cupboard/wiki/Saving-to-the-database)
* [Cupboard talk] (https://skillsmatter.com/skillscasts/4806-simple-persistence-with-cupboard)