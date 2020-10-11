package com.rahul.rickandmorty.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

class AppPreferences() {

    init {
        CLASSES.add(String::class.java)
        CLASSES.add(Boolean::class.java)
        CLASSES.add(Int::class.java)
        CLASSES.add(Long::class.java)
        CLASSES.add(Float::class.java)
        CLASSES.add(MutableSet::class.java)
    }

    private fun AppPreferences() {}


    companion object {
        private val CLASSES: MutableList<Class<*>> = ArrayList()
        private var prefs: SharedPreferences? = null  // cache

        private fun getPrefs(ctx: Context): SharedPreferences? {
            // synchronized is really needed or volatile is all I need (visibility)
            // the same instance of SharedPreferences will be returned AFAIC
            var result = prefs
            if (result == null) synchronized(AppPreferences::class.java) {
                result = prefs
                if (result == null) {
                    prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
                    result = prefs
                }
            }
            return result
        }

        /**
         * Wrapper around {@link SharedPreferences.Editor}
         * {@code put()} methods. Null keys are not permitted. Attempts to insert a
         * null key will throw NullPointerException. Will call
         * {@link SharedPreferences.Editor#apply()} in Gingerbread
         * and above instead of commit. If you want to check the return value call
         * {@link #commit(Context, String, Object)}. When you call this method from
         * different threads the order of the operations is unspecified - you have
         * to synchronize externally if the order concerns you (especially for the
         * same key). If you want to put a long you must explicitly declare it
         * otherwise Java will interpret it as an Integer resulting in a
         * {@link ClassCastException} when you try to retrieve it (on get()
         * invocation). So :
         *
         * <pre>
         * put(ctx, LONG_KEY, 0); // you just persisted an Integer
         * get(ctx, LONG_KEY, 0L); // CCE here
         * put(ctx, LONG_KEY, 0L); // Correct, always specify you want a Long
         * get(ctx, LONG_KEY, 0L); // OK
         * </pre>
         * <p>
         * You will get an {@link IllegalArgumentException} if the value is not an
         * instance of String, Boolean, Integer, Long, Float or Set<String> (see
         * below). This includes specifying a Double mistakenly thinking you
         * specified a Float. So :
         *
         * <pre>
         * put(ctx, FLOAT_KEY, 0.0); // IllegalArgumentException, 0.0 it's a Double
         * put(ctx, FLOAT_KEY, 0.0F); // Correct, always specify you want a Float
         * </pre>
         * <p>
         * You will also get an IllegalArgumentException if you are trying to ADD a
         * Set<String> before API 11 (HONEYCOMB). You **can** persist a {@link Set}
         * that does not contain Strings using this method, but you are recommended
         * not to do_antarctida so. It is untested and the Android API expects a Set<String>.
         * You can actually do_antarctida so in the framework also but you will have raw and
         * unchecked warnings. Here you get no warnings - you've been warned. TODO :
         * clarify/test this behavior
         * <p>
         * Finally, adding null values is supported - but keep in mind that:
         * <ol>
         * <li>you will get a NullPointerException if you put a null Boolean, Long,
         * Float or Integer and you then get() it and assign it to a primitive
         * (boolean, long, float or int). This is *not* how the prefs framework
         * works - it will immediately throw NullPointerException (which is better).
         * TODO : simulate this behavior</li>
         *
         * <li>you can put a null String or Set - but you will not get() null back
         * unless you specify a null default. For non null default you will get this
         * default back. This is in tune with the prefs framework</li>
         * </ol>
         *
         * @param ctx   the context the Shared preferences belong to
         * @param key   the preference's key, must not be {@code null}
         * @param value an instance of String, Boolean, Integer, Long, Float or
         *              Set<String> (for API >= HONEYCOMB)
         * @throws IllegalArgumentException if the value is not an instance of String, Boolean, Integer,
         *                                  Long, Float or Set<String> (including the case when you
         *                                  specify a double thinking you specified a float, see above)
         *                                  OR if you try to ADD a Set<String> _before_ HONEYCOMB API
         * @throws NullPointerException     if key is {@code null}
         */
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        fun <T> put(ctx: Context?, key: String?, value: T) {
            val ed: Editor? = ctx?.let { _put(it, key, value) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
                ed?.apply()
            else
                ed?.commit()
        }

        fun <T> commit(ctx: Context?, key: String?, value: T): Boolean? {
            return ctx?.let { _put(it, key, value)?.commit() }
        }

        @SuppressLint("CommitPrefEdits")
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private fun <T> _put(ctx: Context, key: String?, value: T?): Editor? {
            if (key == null) throw NullPointerException("Null keys are not permitted")
            val ed: Editor? = getPrefs(ctx)?.edit()
            when (value) {
                null -> {
                    // commit it as that is exactly what the API does (but not for boxed
                    // primitives) - can be retrieved as anything but if you give get()
                    // a default non null value it will give this default value back
                    ed?.putString(key, null)
                    // btw the signature is given by the compiler as :
                    // <Object> void
                    // gr.uoa.di.android.helpers.AppPreferences.put(Context ctx,
                    // String key, Object value)
                    // if I write AppPreferences.put(ctx, "some_key", null);
                }
                is String -> ed?.putString(key, value as String?)
                is Boolean -> ed?.putBoolean(key, (value as Boolean?)!!)
                is Int -> ed?.putInt(key, (value as Int?)!!)
                is Long -> ed?.putLong(key, (value as Long?)!!)
                is Float -> ed?.putFloat(key, (value as Float?)!!)
                is Set<*> -> {
                    require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ("You can ADD sets in the preferences only after API "
                                + Build.VERSION_CODES.HONEYCOMB)
                    }
                    val dummyVariable = ed?.putStringSet(key, value as Set<String?>?)
                }
                else -> throw IllegalArgumentException(
                    "The given value : " + value
                            + " cannot be persisted"
                )
            }
            return ed
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        operator fun <T> get(ctx: Context?, key: String?, defaultValue: T?): T? {
            if (key == null)
                throw java.lang.NullPointerException("Null keys are not permitted")
            // if the value provided as defaultValue is null I can't get its class
            return if (defaultValue == null) {
                // if the key !exist I return null which is both the default value
                // provided and what Android would do_antarctida (as in return the default
                // value - except if boxed primitive..)
                if (!ctx?.let { getPrefs(it)?.contains(key) }!!)
                    return null
                // if the key does exist I get the value and..
                val value: Any = getPrefs(ctx)?.all?.get(key) ?: return null
                // ..if null I return null - here I differ from framework - I return
                // null for boxed primitives
                // ..if not null I get the class of the non null value. Here I
                // differ from framework - I do_antarctida not throw if the (non null) value is
                // not of the type the variable to receive it is - cause I have no
                // way to guess the return value expected ! (***)
                val valueClass: Class<*> = value.javaClass
                // the order of "instanceof" checks does not matter - still if I
                // have a long autoboxed as Integer ? - tested in
                // testAPNullDefaultUnboxingLong() and works OK (long 0L is
                // autoboxed as long)
                for (cls in CLASSES) {
                    if (valueClass.isAssignableFrom(cls)) {
                        // try {
                        // I can't directly cast to T as value may be boolean
                        // for instance
                        return valueClass.cast(value) as T?
                        // } catch (ClassCastException e) { // won't work see :
                        // //
                        // http://stackoverflow.com/questions/186917/
                        // (how-do_antarctida-i-catch-classcastexception)
                        // // basically the (T) valueClass.cast(value); line is
                        // // translated to (Object) valueClass.cast(value); which
                        // // won't fail ever - the CCE is thrown in the assignment
                        // // (T t =) String s = AppPreferences.get(this, "key",
                        // // null); which is compiled as
                        // // (String) AppPreferences.get(this, "key",
                        // // null); and get returns an Integer for instance
                        // String msg = "Value : " + value + " stored for key : "
                        // + key
                        // + " is not assignable to variable of given type.";
                        // throw new IllegalStateException(msg, e);
                        // }
                    }
                }
                throw IllegalStateException("Unknown class for value :\n\t$value\nstored in preferences")

            } else if (defaultValue is String)
                ctx?.let { getPrefs(it)?.getString(key, defaultValue as String?) } as T
            else if (defaultValue is Boolean)
                ctx?.let { getPrefs(it)?.getBoolean(key, (defaultValue as Boolean?)!!) } as T
            else if (defaultValue is Int)
                ctx?.let { getPrefs(it)?.getInt(key, (defaultValue as Int?)!!) } as T
            else if (defaultValue is Long)
                ctx?.let { getPrefs(it)?.getLong(key, (defaultValue as Long?)!!) } as T
            else if (defaultValue is Float)
                ctx?.let { getPrefs(it)?.getFloat(key, (defaultValue as Float?)!!) } as T
            else if (defaultValue is Set<*>) {
                require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    ("You can ADD sets in the preferences only after API "
                            + Build.VERSION_CODES.HONEYCOMB)
                }
                // this set can contain whatever it wants - don't be fooled by the
                // Set<String> cast
                ctx?.let { getPrefs(it)?.getStringSet(key, defaultValue as Set<String?>?) } as T
            } else
                throw java.lang.IllegalArgumentException("$defaultValue cannot be persisted in SharedPreferences")
        }

        /**
         * Wraps {@link SharedPreferences#contains(String)}.
         *
         * @param ctx the context the SharedPreferences belong to
         * @param key the preference's key, must not be {@code null}
         * @return true if the preferences contain the given key, false otherwise
         * @throws NullPointerException if key is {@code null}
         */
        fun contains(ctx: Context?, key: String?): Boolean {
            if (key == null) throw java.lang.NullPointerException("Null keys are not permitted")
            return ctx?.let { getPrefs(it)?.contains(key) } as Boolean
        }

        /**
         * Wraps {@link SharedPreferences#getAll()}. Since you must
         * not modify the collection returned by this method, or alter any of its
         * contents, this method returns an <em>unmodifiableMap</em> representing
         * the preferences.
         *
         * @param ctx the context the SharedPreferences belong to
         * @return an <em>unmodifiableMap</em> containing a list of key/value pairs
         * representing the preferences
         * @throws NullPointerException as per the docs of getAll() - does not say when
         */
        fun getAll(ctx: Context?): Map<String?, *>? {
            return Collections.unmodifiableMap(ctx?.let { getPrefs(it)?.all })
        }

        /**
         * Wraps {@link SharedPreferences.Editor#clear()}. See its
         * docs for clarifications. Calls
         * {@link SharedPreferences.Editor#commit()}
         *
         * @param ctx the context the SharedPreferences belong to
         * @return true if the preferences were successfully cleared, false
         * otherwise
         */
        fun clear(ctx: Context?): Boolean {
            return ctx?.let { getPrefs(it)?.edit()?.clear()?.commit() } as Boolean
        }

        /**
         * Wraps {@link SharedPreferences.Editor#remove(String)}.
         * See its docs for clarifications. Calls
         * {@link SharedPreferences.Editor#commit()}.
         *
         * @param ctx the context the SharedPreferences belong to
         * @param key the preference's key, must not be {@code null}
         * @return true if the key was successfully removed, false otherwise
         * @throws NullPointerException if key is {@code null}
         */
        fun remove(ctx: Context?, key: String?): Boolean {
            if (key == null)
                throw java.lang.NullPointerException("Null keys are not permitted")
            return ctx?.let { getPrefs(it)?.edit()?.remove(key)?.commit() } as Boolean
        }

        /**
         * Wraps
         * {@link SharedPreferences#registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener)}
         * .
         *
         * @param ctx the context the SharedPreferences belong to
         * @param lis the listener, must not be null
         * @throws NullPointerException if lis is {@code null}
         */
        fun registerListener(ctx: Context?, lis: OnSharedPreferenceChangeListener?) {
            if (lis == null)
                throw java.lang.NullPointerException("Null listener")
            ctx?.let { getPrefs(it)?.registerOnSharedPreferenceChangeListener(lis) }
        }

        /**
         * Wraps
         * {@link SharedPreferences#unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener)}
         * .
         *
         * @param ctx the context the SharedPreferences belong to
         * @param lis the listener, must not be null
         * @throws NullPointerException if lis is {@code null}
         */
        fun unregisterListener(ctx: Context?, lis: OnSharedPreferenceChangeListener?) {
            if (lis == null)
                throw java.lang.NullPointerException("Null listener")
            ctx?.let { getPrefs(it)?.unregisterOnSharedPreferenceChangeListener(lis) }
        }

        /**
         * Wraps
         * {@link SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(SharedPreferences, String)}
         * .
         *
         * @param ctx the context the SharedPreferences belong to
         * @param lis the listener, must not be null
         * @param key the key we want to run onSharedPreferenceChanged on, must not
         *            be null
         * @throws NullPointerException if lis or key is {@code null}
         */
        fun callListener(ctx: Context?, lis: OnSharedPreferenceChangeListener?, key: String?) {
            if (lis == null)
                throw java.lang.NullPointerException("Null listener")
            if (key == null)
                throw java.lang.NullPointerException("Null keys are not permitted")
            lis.onSharedPreferenceChanged(ctx?.let { getPrefs(it) }, key)
        }

        /**
         * Check that the given set contains strings only.
         *
         * @param set
         * @return the set cast to Set<String>
         */
        @SuppressWarnings("unused")
        private fun checkSetContainsStrings(set: Set<*>): Set<String>? {
            if (set.isNotEmpty()) {
                for (`object` in set) {
                    require(`object` is String) { "The given set does not contain strings only" }
                }
            }
            return set as Set<String>
        }


    }
}