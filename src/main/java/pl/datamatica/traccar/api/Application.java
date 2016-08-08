package pl.datamatica.traccar.api;

import spark.Spark;


public class Application implements spark.servlet.SparkApplication {
	
	@Override
	public void init() {
		Spark.get("test", (req, res) -> {
			return "Hello world";
		});
	}
}