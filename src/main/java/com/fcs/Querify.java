package com.fcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.TableResult;

public class Querify {

	BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
	Logger log = Logger.getLogger(Querify.class.getName());

	String datasetName;
	
	private Querify() {

	}
	
	public Querify(String datasetName) {
		// TODO Auto-generated constructor stub
		dataset(datasetName);
	}
	public Querify dataset(String datasetName) {
		this.datasetName = datasetName;
		return this;
	}

	public static Querify getInstance() {
		return new Querify();
	}
	public static Querify getInstance(String datasetName) {
		return new Querify(datasetName);
	}

	public void createDataSet(String datasetName) {
		this.datasetName = datasetName;
		Dataset dataset = null;
		DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();

		dataset = bigquery.create(datasetInfo);

		log.fine(String.format("Dataset %s created.%n", dataset.getDatasetId().getDataset()));
	}

	

	public void createTable(Class c) {

		String datasetName = "cec";
		TableId tableName = TableId.of(datasetName, c.getSimpleName());

		ArrayList<Field> fieldList = new ArrayList<Field>();
		for (java.lang.reflect.Field f : c.getDeclaredFields()) {

			String fType = f.getType().getSimpleName().toUpperCase();
			if (fType.equalsIgnoreCase("LONG")) {
				fType = LegacySQLTypeName.INTEGER.toString();
			}

			String fName = f.getName();
			fieldList.add(Field.of(fName, LegacySQLTypeName.valueOf(fType)));
			System.out.println(fName + ":" + fType);
		}

		Schema schema = Schema.of(fieldList);

		TableDefinition tableDefinition = StandardTableDefinition.of(schema);
		TableInfo tableInfo = TableInfo.newBuilder(tableName, tableDefinition).build();
		this.bigquery.create(tableInfo);

	}
	public Map<String, Object> objectToMap(Object o)
	{
		Map<String, Object> recordsContent = new HashMap<String, Object>();
		Class c = o.getClass();
		for (java.lang.reflect.Field f : c.getDeclaredFields()) {
			f.setAccessible(true);
			String fType = f.getType().getSimpleName().toUpperCase();
			if (fType.equalsIgnoreCase("LONG")) {
				fType = LegacySQLTypeName.INTEGER.toString();
			}

			String fName = f.getName();
			try {
				recordsContent.put(fName, f.get(o));
				log.fine(c.getSimpleName()+"."+fName + ":" + f.get(o));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return recordsContent;
	}
	public  <T> void insert(T... objs) {

		
		Class<? extends Object> c = objs[0].getClass();
		TableId tableName = TableId.of(this.datasetName, c.getSimpleName());

		ArrayList<Field> fieldList = new ArrayList<Field>();
		for (java.lang.reflect.Field f : c.getDeclaredFields()) {

			String fType = f.getType().getSimpleName().toUpperCase();
			if (fType.equalsIgnoreCase("LONG")) {
				fType = LegacySQLTypeName.INTEGER.toString();
			}

			String fName = f.getName();
			fieldList.add(Field.of(fName, LegacySQLTypeName.valueOf(fType)));
			log.fine(fName + ":" + fType);
		}

		Schema schema = Schema.of(fieldList);

		TableDefinition tableDefinition = StandardTableDefinition.of(schema);
		TableInfo tableInfo = TableInfo.newBuilder(tableName, tableDefinition).build();
		
		ArrayList<RowToInsert> rows = new ArrayList<RowToInsert>();
		
		for (int i = 0; i < objs.length; i++) {
			RowToInsert row = RowToInsert.of(this.objectToMap(objs[i]) );
			rows.add(row);
		}
		
		
		InsertAllRequest insertAll = InsertAllRequest.of(tableName, rows);
		
		this.bigquery.insertAll(insertAll);

	}
	public  TableResult query(String sql) throws JobException, InterruptedException {
		
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(sql).build();
		return this.bigquery.query(queryConfig);
	
		
	}

	public static void main(String[] args) {


		try {
			TableResult result = Querify.getInstance("cec").query("SELECT * FROM `crazy-english-community.cec.MemberPost` LIMIT 1000");
			for (FieldValueList row :result.iterateAll()) {
				
		        System.out.println(row.get("id").getStringValue());
		      }


		} catch (JobException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

	}

}
