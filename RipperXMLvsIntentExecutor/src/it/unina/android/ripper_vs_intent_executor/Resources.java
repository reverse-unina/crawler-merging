package it.unina.android.ripper_vs_intent_executor;

import com.nofatclips.crawler.Prefs;
import com.nofatclips.crawler.model.Comparator;
import com.nofatclips.crawler.model.ResourceFile;
import com.nofatclips.crawler.strategy.comparator.*;

public class Resources implements ResourceFile {

//      public static Comparator COMPARATOR = new CustomWidgetsComparator(CustomWidgetsComparator.IGNORE_ACTIVITY_NAME, EDIT_TEXT, BUTTON, LIST_VIEW, MENU_VIEW, IMAGE_VIEW);
        public static String COMPARATOR_TYPE = "NullComparator";
        public static String[] WIDGET_TYPES = {};
        public static boolean COMPARE_ACTIVITY_NAME = true;
        public static boolean COMPARE_STATE_TITLE = false;
        public static boolean COMPARE_LIST_COUNT = false;
        public static boolean COMPARE_MENU_COUNT = true;
        public static boolean COMPARE_VALUES = false;
        
        public static void getComparator() {            
                if (COMPARATOR_TYPE.equals("") || (COMPARATOR_TYPE == null)) return;

                if (COMPARATOR_TYPE.equals("CustomWidgetsComparator")) {
                        COMPARATOR = new CustomWidgetsComparator (WIDGET_TYPES);
                } else if (COMPARATOR_TYPE.equals("CustomWidgetsDeepComparator")) {
                        COMPARATOR = new CustomWidgetsDeepComparator (WIDGET_TYPES);
                } else if (COMPARATOR_TYPE.equals("NameComparator")) {
                        COMPARATOR = new NameComparator ();
                } else {
                        try {
                                String className = (COMPARATOR_TYPE.indexOf(".") == -1)?
                                                ("com.nofatclips.crawler.strategy.comparator." + COMPARATOR_TYPE):COMPARATOR_TYPE;
                                Class<?> theClass = Class.forName(className);
                                COMPARATOR = (Comparator) theClass.newInstance();
                        } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        } catch (IllegalAccessException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }

                }
                
        }
        
        public static Comparator COMPARATOR = new NullComparator();
        static {
                Prefs.updateNode("comparator", Resources.class);
                getComparator();
        }

}