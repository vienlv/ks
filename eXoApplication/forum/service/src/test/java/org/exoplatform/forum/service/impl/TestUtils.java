/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.service.impl;

import static org.exoplatform.ks.test.AssertUtils.assertContains;
import static org.exoplatform.ks.test.AssertUtils.assertEmpty;
import static org.exoplatform.ks.test.AssertUtils.assertNotContains;
import static org.exoplatform.ks.test.mock.JCRMockUtils.stubValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;

import junit.framework.TestCase;

import org.exoplatform.forum.service.Utils;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestUtils extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testRemoveCharterStrange() {

    assertEquals("",  Utils.removeCharterStrange(null));
    assertEquals("", Utils.removeCharterStrange(""));
    
    String s = "aaa"+ new String(Character.toChars(31)) + "bbb";
    assertEquals("aaabbb", Utils.removeCharterStrange(s));
    
    
    String s2 = "aaa"+ new String(Character.toChars(32)) + "bbb";
    assertEquals("aaa" + s2.charAt(3) +"bbb", Utils.removeCharterStrange(s2));
    
  }

  public void testArraysHaveDifferentContent() {
    
   // if 2 arrays are the same, then it's false
   String [] a = new String [] {"foo","bar", "zed"};
   String [] b = new String [] {"foo","bar", "zed"};
   assertFalse (Utils.arraysHaveDifferentContent(a, b));
   
   // order is not important
   a = new String [] {"foo","bar", "zed"};
   b = new String [] {"zed", "foo", "bar"};
   assertFalse(Utils.arraysHaveDifferentContent(a, b));
   
   // if there is a difference in size, it's true
   a = new String [0];
   b = new String [] {"foo","bar", "zed"};
   assertTrue(Utils.arraysHaveDifferentContent(a, b));
   
   // if there is a difference in content, it's true
   a = new String [] {"foo","bar", "zed*"};
   b = new String [] {"foo","bar", "zed"};
   assertTrue(Utils.arraysHaveDifferentContent(a, b));
   
   // if there is a difference in content, it's true
   a = new String [] {"foo","bar", "zed"};
   b = new String [] {"foo","bar", "zed*"};
   assertTrue(Utils.arraysHaveDifferentContent(a, b));
   
  }

  public void testListsHaveDifferentContent() {
    // if 2 arrays are the same, then it's false
    List<String> a = Arrays.asList("foo","bar", "zed");
    List<String> b = Arrays.asList("foo","bar", "zed");
    assertFalse (Utils.listsHaveDifferentContent(a, b));
    
    // order is not important
    a = Arrays.asList("foo","bar", "zed");
    b = Arrays.asList("zed", "foo", "bar");
    assertFalse(Utils.listsHaveDifferentContent(a, b));
    
    // if there is a difference in size, it's true
    a = Arrays.asList(new String[0]);
    b = Arrays.asList("foo","bar", "zed");
    assertTrue(Utils.listsHaveDifferentContent(a, b));
    
    // if there is a difference in content, it's true
    a = Arrays.asList("foo","bar", "zed*");
    b = Arrays.asList("foo","bar", "zed");
    assertTrue(Utils.listsHaveDifferentContent(a, b));
    
    // if there is a difference in content, it's true
    a = Arrays.asList("foo","bar", "zed");
    b = Arrays.asList("foo","bar", "zed*");
    assertTrue(Utils.listsHaveDifferentContent(a, b));
  }

  public void testMapToArray() {
    Map<String,String> map = new HashMap<String,String>();
    String [] actual = Utils.mapToArray(map);
    assertContains(actual, " ");
    
    map.put("foo", "foo");
    map.put("bar", "bar");
    map.put("zed", "zed");
    actual = Utils.mapToArray(map);
    assertContains(actual, "foo,foo", "bar,bar", "zed,zed");
  }

  public void testArrayToMap() {
    String [] s = new String [] {"foo,foo", "bar,bar", "zed,zed"};
    Map<String,String> actual = Utils.arrayToMap(s);
    assertContains(actual.keySet(), "foo", "bar", "zed");
    assertEquals("foo", actual.get("foo"));
    assertEquals("bar", actual.get("bar"));
    assertEquals("zed", actual.get("zed"));

    
    s = new String [] {"foo", "bar,bar", "zed,zed,zed"};
    actual = Utils.arrayToMap(s);
    assertContains(actual.keySet(), "bar");
    assertEquals(null, actual.get("foo"));
    assertEquals("bar", actual.get("bar"));
    assertEquals(null, actual.get("zed")); 
  }

  public void testGetQueryInList() {
   List<String> list = new ArrayList<String>(); 
   String actual = Utils.propertyMatchAny("prop", list);
   assertEquals("", actual);
   
   list = Arrays.asList("foo");
   actual = Utils.propertyMatchAny("prop", list);
   assertEquals("(not(prop) or prop='foo')", actual);
   
   list = Arrays.asList("foo", "bar");
   actual = Utils.propertyMatchAny("prop", list);
   assertEquals("(not(prop) or prop='foo' or prop='bar')", actual);
   
   list = Arrays.asList("foo", "bar", "zed");
   actual = Utils.propertyMatchAny("prop", list);
   assertEquals("(not(prop) or prop='foo' or prop='bar' or prop='zed')", actual);
   
  }

  public void testIsListContentItemList() {
    List<String> list = Arrays.asList(" ");
    List<String> list1 = Arrays.asList(" ");
    boolean actual = Utils.isListContentItemList(list, list1);
    assertFalse(actual);
    
    list = Arrays.asList("bar", "zed");
    list1 = Arrays.asList("foo");
    actual = Utils.isListContentItemList(list, list1);
    assertFalse(actual);
    
  }

  public void testGetStringsInList()  throws Exception {
    List<String> list = new ArrayList<String>();
    String [] actual = Utils.getStringsInList(list);
    assertEquals(0, actual.length);
    list.add("foo");
    list.add(" ");
    list.add("bar");
    actual = Utils.getStringsInList(list);
    assertNotContains(Arrays.asList(actual), " ");
    assertContains(actual, "foo", "bar");
  }

  public void testExtractSameItems() throws Exception {
    List<String> pList = Arrays.asList("foo", "bar", "zed");
    List<String> cList = Arrays.asList("foo", " ", "bar");
    List<String>  actual = Utils.extractSameItems(pList, cList);
    assertContains(actual, "foo", "bar");
    
    // verify behaviour if first list is empty
    pList = new ArrayList<String>();
    cList = Arrays.asList("foo", " ", "bar");
    actual = Utils.extractSameItems(pList, cList);
    assertEmpty(actual);
    
    
    // verify behaviour if no common elements
    pList = Arrays.asList("foo", "bar", "zed");
    cList = Arrays.asList("foo*", "bar*", "zed*");
    actual = Utils.extractSameItems(pList, cList);
    assertEmpty(actual);
    
  }

  public void testValuesToArray() throws Exception {
    Value[] values = new Value [0] ;
    String [] actual = Utils.valuesToArray(values);
    assertEquals(0, actual.length);
    
    values = new Value [] {stubValue("foo")};
    actual = Utils.valuesToArray(values);
    assertContains(actual, "foo");
    assertEquals(1, actual.length);
    
    values = new Value [] {stubValue("foo"),stubValue("bar"),stubValue("zed")};
    actual = Utils.valuesToArray(values);
    assertContains(actual, "foo", "bar", "zed");
    assertEquals(3, actual.length);
  }

  public void testValuesToList()  throws Exception {
    Value[] values = new Value [0] ;
    List<String> actual = Utils.valuesToList(values);
    assertEmpty(actual);
    
    values = new Value [] {stubValue("foo")};
    actual = Utils.valuesToList(values);
    assertContains(actual, "foo");
    assertEquals(1, actual.size());
    
    values = new Value [] {stubValue("foo"),stubValue("bar"),stubValue("zed")};
    actual = Utils.valuesToList(values);
    assertContains(actual, "foo", "bar", "zed");
    assertEquals(3, actual.size());
  }
  
  public void testArrayCopy() {
    
    // null in, null out
    String [] source = null;
    String [] actual = Utils.arrayCopy(source);
    assertNull(actual);

    // empty array
    source = new String[0];
    actual = Utils.arrayCopy(source);
    assertNotNull(actual);
    assertEquals(0, actual.length);
    
    source = new String [] {"foo", "bar", "zed"};
    actual = Utils.arrayCopy(source);
    assertEquals("copied arrays should have same size", source.length, actual.length);
    assertNotSame("a new array should have been created", source, actual);
    assertContains(actual, "foo", "bar", "zed"); // should contain all elements
  }
  
  public void testCodeLink() {
  	String link = "http://domain.com/test/id12412";
  	link = Utils.encodeLink(link);
  	assertEquals("http58%47%47%domain.com47%test47%id12412", link);
  	
	  link = Utils.uncodeLink(link);
	  assertEquals("http://domain.com/test/id12412", link);
  }
  

}