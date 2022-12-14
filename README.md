# Souvenirs

### Storage

According to a task, we can store information about manufacturers and their products (souvenirs) in files of any format.

Therefore, we will  store them in data store which would be represented by directory on filesystem.

Directory name is `souvenirs` (could be considered as database)

This directory will contain subdirectories `manufacturers` and `souvenirs` (could be considered as database tables)

Each subdirectory will contain a set of JSON files with file extension `.json`. Each JSON file is a single record of
such database (eg one manufacturer or one souvenir in it).

Each JSON file will contain field `id` (mandatory) which is numerical and can not be `0`. Value of `id` field is a file
name of JSON file (plus file extension `.json`)

Each souvenir record will also contain field `manufacturerId` which is `id` of some record inside 

### App configuration file

Configuration file `config.properties` contain property

    `path` which is a full path to data store root directory.

This directory must be writable by user, who will run application. It will contain data store `souvenirs`

### Menu structure

Menu contains 2 sections:

1. Main menu which accessible right after app start. It contains basic CRUD functionality  for both Manufacturers
and Souvenirs
2. Menu "Other..." which contains task specific routines.

### Patterns

There were used at least 2 patterns:

1. Visitor pattern (set of data filters)
2. Factory method - method `DataTableFactory` in classes `ManufacturersTable` and `SouvenirsTable`

