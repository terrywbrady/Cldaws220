The following code wokes for db.listGlobalTables() but not db.listTables()

```
exports.handler = async (event) => {
    console.log(event);
    var AWS = require("aws-sdk");
    AWS.config.update({region: 'us-west-2'});
    AWS.config.apiVersions = {
      dynamodb: '2012-08-10',
    };
    //var queues = new AWS.SQS({apiVersion: '2012-11-05'}).listQueues();
    var db = new AWS.DynamoDB();
    var tables = [];
    db.listTables({}, function(err, data){
      console.log("one "+ err);
      console.log("one "+ data);
      if (err) {
          console.log("two");
          console.log(err, err.stack); // an error occurred
      } else {
          console.log("three");
          console.log(data);
          tables = data;
      }
    });
    console.log("four");

    console.log(tables);
    var x = event.x ? event.x * event.x : 0;
    const response = {
        statusCode: 200,
        body: JSON.stringify('Hello from Lambda!'),
        square: x,
        tables: JSON.stringify(tables),
    };
    return response;
};
```

Processing API Gateway Query queryStringParameters

Note: only return statusCode and body
```
var foo = event.queryStringParameters ? event.queryStringParameters.foo : "---";

```
