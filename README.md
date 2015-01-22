# cupboardTestApp
A test app I have written to demonstrate how to use the cupboard SQLite helper for android

Cupboard guide for CodePath
                                                ## Overview

Cupboard is a way to manage persistence in a sqlite instance for your app. It's a small library, simple to use, and it was designed specifically for Android unlike ORMlite.

Using the ActiveAndroid ORM makes managing client-side models extremely easy in simple cases. For more advanced or custom cases, you can use [[SQLiteOpenHelper|Local-Databases-with-SQLiteOpenHelper]] to manage the database communication directly. But for simple model mapping from JSON, ActiveAndroid keeps things simple.  

<img src="include some picture here" width="500" alt="orm" />

Note that while this library does not enforce the DOA model, we are going to leverage it regardless as it adds to overall code clarity. 

<!-- 
ActiveAndroid works like an **Object Relational Mapper** by mapping java classes to database tables and mapping java class member variables to the table columns. Through this process, **each table** maps to a **Java model** and **the columns** in the table represent the respective **data fields**. Similarly, each row in the database represents a particular object. This allows us to create, modify, delete and query our SQLite database using model objects instead of raw SQL.

For example, a "Tweet" model would be mapped to a "tweets" table in the database. The Tweet model might have a "body" field that maps to a body column in the table and a "timestamp" field that maps to a timestamp column. Through this process, each row would map to a particular tweet. -->

### Installation

To install Cupboar, simply add the line

```groovy
compile 'nl.qbusict:cupboard:{latest version number}'
```

to the dependencies section of your app's build.gradle file.
Recompile your project, and you should be done. 


                                              ### Configuration

Next, we'll setup a custom SQLiteOpenHelper. This is a standard object in the Android framework that assists in dealing with SQLite databases. For now, we'll just create the object and register one POJO in our database: `Bunny`
```java
package mikecanco.de.cupboardtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static nl.qbusict.cupboard.CupboardFactory.cupboard;
/**
 * Created by koalahamlet on 1/20/15.
 */
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
        // add indexes and other database tweaks if you want

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work

    }

}

```
                                                                                                                                                        
**Note** that you must either directly use the `com.activeandroid.app.Application` as your application class (specified in the manifest) or if you have a custom application class, check out more details for how to approach that in the [installation guide](https://github.com/pardom/ActiveAndroid/wiki/Getting-started). Now you are ready to use ActiveAndroid. 

                                                ### Usage

                            #### Defining Models

First, we define our models by...

```java

```

The "name" part of the annotations refers to the name the Table or Columns will be given, so make sure to use the SQLite naming conventions for those. Also note that **ActiveAndroid creates a local id (Id)** in addition to our manually managed `remoteId` (unique) which is the id on the server (for networked applications). To access that primary key Id, you can call `getId()` on an instance of your model.

#### CRUD Operations

(this section needs to be longer for cupboard)

Now we can create, modify and delete records for these models backed by SQLite:

```java
// Create a category
Category restaurants = new Category();
restaurants.remoteId = 1;
restaurants.name = "Restaurants";
restaurants.save();

// Create an item 
Item item = new Item();
item.remoteId = 1;
item.category = restaurants;
item.name = "Outback Steakhouse";
item.save();

// Deleting items
Item item = Item.load(Item.class, 1);
item.delete();
// or with
new Delete().from(Item.class).where("remote_id = ?", 1).execute();
```

                            #### Querying Records

We can query records with a simple query syntax `get` method....

```java

```

That's ActiveAndroid in a nutshell. 

                            #### Executing Custom SQL

To run custom SQL with no need for a result, use the `SQLiteUtils.execSql` method:

```java
// Note nothing is returned from this
SQLiteUtils.execSql("DELETE FROM table_name");
```

If you need to execute a custom query and want to get a `List` of items back use `SQLiteUtils.rawQuery`:

```java
List<TodoItem> importantItems = 
  SQLiteUtils.rawQuery(TodoItem.class, 
     "SELECT * from todo_items where priority = ?", 
     new String[] { "high" });
```



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

Review this [[Custom CursorAdapter and ListViews|Populating a ListView with a CursorAdapter]] guide in order to load content from a `Cursor` into a `ListView`. In summary, in order to populate a `ListView` directly from the content within the ActiveAndroid SQLite database, we can define this method on the model to retrieve a `Cursor` for the result set:

```java
public class TodoItem extends Model {
    // ...

    // Return cursor for result set for all todo items
    public static Cursor fetchResultCursor() {
        String tableName = Cache.getTableInfo(TodoItem.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id").
            from(TodoItem.class).toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
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

That's all we have to do to load data from ActiveAndroid directly through a `Cursor` into a list.

                            #### Loading with Content Providers

Instead of using the underlying SQLite database directly, we can instead expose the ActiveAndroid data as a content provider with a few simple additions. First, override the default identity column for all ActiveAndroid models:

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
              android:name="com.activeandroid.content.ContentProvider" />
    ...
</application>
```

See the full source code on the official [ActiveAndroid ContentProviders guide](https://github.com/pardom/ActiveAndroid/wiki/Using-the-content-provider).

                                                #### Official Reference Guides

Check these official reference guides for more detailed information:

 * [Install ActiveAndroid and perform initial setup](https://github.com/pardom/ActiveAndroid/wiki/Getting-started)
 * [Define your application models](https://github.com/pardom/ActiveAndroid/wiki/Creating-your-database-model)
 * [Persist data using save](https://github.com/pardom/ActiveAndroid/wiki/Saving-to-the-database)
 * [Query the local database](https://github.com/pardom/ActiveAndroid/wiki/Querying-the-database)
 * [Perform data schema migrations](https://github.com/pardom/ActiveAndroid/wiki/Schema-migrations)

Be sure to review the common questions below.

## Common Questions

> Question: How does Cupboard handle duplicate IDs? For example, I want to make sure no duplicate twitter IDs are inserted. Is there a way to specify a column is the primary key in the model?


> Question: How do you specify the data type (int, text)?  Does Cupboard automatically know what the column type should be?

The type is inferred automatically from the type of the field.

> Question: How do I store dates into Cupboard?


> Question: How do you represent a 1-1 relationship?

This is a good one to note. common use case that you're not sure of yet with cupboard.

<!-- 
Check out the [relationships section](https://github.com/pardom/ActiveAndroid/wiki/Creating-your-database-model#relationships) if you haven't yet. There are many ways to manage this. You can declare a type "User" and then that field will be associated with a [foreign key representing the user](https://github.com/pardom/ActiveAndroid/blob/master/src/com/activeandroid/Model.java#L135). 

```java
public class User extends Model {
  public String email;
  public Address address;
}
```

You can manage this process by simply constructing and assigning the user object to a field of the parent object and then calling save on the parent. 

```java
User u = new User("bob@foo.com");
u.address = new Address("135 Hesby St, Los Angeles, CA");
u.address.save();
u.save();
``` -->

> Question: How do I delete all the records from a table?


> Question: Is it possible to do joins with ActiveAndroid? 


> Question: What are the best practices when interacting with the sqlite in Android, is ORM/DAO the way to go?


## References

* [Cupboard bitbucket](https://bitbucket.org/qbusict/cupboard)
* [AA Getting Started](https://github.com/pardom/ActiveAndroid/wiki/Getting-started)
* [AA Models](https://github.com/pardom/ActiveAndroid/wiki/Creating-your-database-model)
* [AA Saving](https://github.com/pardom/ActiveAndroid/wiki/Saving-to-the-database)
* [Cupboard talk] (https://skillsmatter.com/skillscasts/4806-simple-persistence-with-cupboard)