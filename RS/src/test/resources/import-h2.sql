-- Dump into user_group table

INSERT INTO user_group (userGroup) VALUES ('Student');


-- Dump into gender table

INSERT INTO gender (gender) VALUES ('männlich'), ('weiblich');


-- Dump into attribute_tupel_tree table

INSERT INTO attribute_tupel_tree (id) VALUES (1);
INSERT INTO attribute_tupel_tree (id) VALUES (2);
INSERT INTO attribute_tupel_tree (id) VALUES (3);
INSERT INTO attribute_tupel_tree (id) VALUES (4);
INSERT INTO attribute_tupel_tree (id) VALUES (5);
INSERT INTO attribute_tupel_tree (id) VALUES (6);
INSERT INTO attribute_tupel_tree (id) VALUES (7);
INSERT INTO attribute_tupel_tree (id) VALUES (8);
INSERT INTO attribute_tupel_tree (id) VALUES (9);
INSERT INTO attribute_tupel_tree (id) VALUES (10);
INSERT INTO attribute_tupel_tree (id) VALUES (13);
INSERT INTO attribute_tupel_tree (id) VALUES (14);
INSERT INTO attribute_tupel_tree (id) VALUES (15);

-- Dump image table
-- Item images
INSERT INTO image (id, filename, image, filetype, uploadedAt) VALUES (1, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/starbucks.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype, uploadedAt) VALUES (2, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/nordsee.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype, uploadedAt) VALUES (3, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/burgerking.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype, uploadedAt) VALUES (4, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/subway.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (5, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/bier.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (6, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/bier.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (7, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/mcdonalds.png'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (8, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/bier.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (9, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/bier.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (10, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/bier.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (11, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/bier.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (12, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/bier.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (13, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/gosch.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (14, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/mr-clou.jpg'), 'image/jpg', NOW());
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (15, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/images/dm.jpg'), 'image/jpg', NOW());

-- Attribute images
INSERT INTO image (id, filename, image, filetype,  uploadedAt) VALUES (16, 'image', LOAD_FILE('/Users/mac/Workspace/Masterarbeit/Aufenthalt/RS/src/main/resources/icons/coffee-cup.png'), 'image/png', NOW());



-- Dump into user table

INSERT INTO user (id, firstname, lastname, dateOfBirth, username, adaptivityEnabled, bufferToNextConnection, currentWalkingSpeed, dateOfRegistration, dateOfLastUpdate, email, isSmoker, isVegan, isVegetarian, idgender) VALUES (1, 'Niclas', 'Kannengießer', '1990-10-21', 'nic', false, 5, 4.3, NOW(), NOW(), 'beispiel@web.de', false, false, false, 1);
INSERT INTO user (id, firstname, lastname, dateOfBirth, username, adaptivityEnabled, bufferToNextConnection, currentWalkingSpeed, dateOfRegistration, dateOfLastUpdate, email, isSmoker, isVegan, isVegetarian, idgender) VALUES (2, 'Jens', 'Hegenberg', '1981-04-26', 'jens', false, 5, 4.3, NOW(), NOW(), 'beispiel2@web.de', false, false, false, 1);


-- Dump into item table

INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idimage) VALUES (3, 'Starbucks Coffe', NOW(), NOW(), false, false, false, false, false, true, 'Normales Cafe, in dem man auch Kekse essen kann.', 1);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idimage) VALUES (4, 'Nordsee', NOW(), NOW(), false, false, false, false, false, true, 'Normales Cafe, in dem man auch Kekse essen kann.', 2);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idimage) VALUES (5, 'Burger King', NOW(), NOW(), false, false, false, false, false, true, 'Amerkanisches FastFood Restaurant', 3);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idimage) VALUES (6, 'Subway', NOW(), NOW(), false, false, false, false, false, true, 'Gehört zu einer Kette von Baguette-Restaurants.', 4);

INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (7, 'Café Cool', NOW(), NOW(), false, false, false, false, false, false, 'Cafe, in dem man auch Kekse essen kann.', 1, 5);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (8, 'Ostsee', NOW(), NOW(), false, false, false, false, false, false, 'Cafe in dem man auch Kekse essen kann.', 2, 6);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (9, 'McDonalds', NOW(), NOW(), false, false, false, false, false, false, 'Amerkanisches FastFood Restaurant', 3, 7);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (10, 'Highway', NOW(), NOW(), false, false, false, false, false, false, 'Gehört zu einer Kette von Baguette-Restaurants.', 4, 8);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (11, 'Café Nenninger', NOW(), NOW(), false, false, false, false, false, false, 'Gemütliches Café mit guter Kuchenauswahl.', 5, 9);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (12, 'Münz-Mallorca', NOW(), NOW(), false, false, false, false, false, false, 'Raucherbar mit Spielautomaten', 6, 10);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (13, 'Gosch', NOW(), NOW(), false, false, false, false, false, false, 'Frischen Fisch kann man sich hier gönnen.', 7, 11);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (14, 'Mr. Clou', NOW(), NOW(), false, false, false, false, false, false, 'Hier kann man eckere Bio Smoothies kaufen.', 8, 12);
INSERT INTO item (id, name, dateOfLastUpdate, dateOfRegistration, hasOutdoorArea, hasSockets, hasWifi, isOutdoor, isProductivityItem, isTrainingItem, description, idgeoposition, idimage) VALUES (15, 'dm', NOW(), NOW(), false, false, false, false, false, false, 'Hier kann man Drogerie-Artikel kaufen.', 9, 12);


-- Dump into attribute table

INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Gastronomie', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Restaurant', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Cafe', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Imbisshalle', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Shopping', 0, 16);


INSERT INTO attribute (id, attribute, minDuration, image_id) VALUES (6, 'Fleisch', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Fisch', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Fastfood', 0, 16);

INSERT INTO attribute (id, attribute, minDuration, image_id) VALUES (9, 'Heißgetränke', 0, 16);
INSERT INTO attribute (id, attribute, minDuration, image_id) VALUES (10, 'Kaffee', 0, 16);

INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Spielautomaten', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Bier', 0, 16);
INSERT INTO attribute (id, attribute, minDuration, image_id) VALUES (13, 'Bio', 0, 16);
INSERT INTO attribute (attribute, minDuration, image_id) VALUES ('Burger', 0, 16);


INSERT INTO attribute (id, attribute, minDuration, image_id) VALUES (15, 'Shopping', 0, 16);
INSERT INTO attribute (id, attribute, minDuration, image_id) VALUES (16, 'Drogerie', 0, 16);


-- Dump into attribute_tupel

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 3);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 3);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (9, 3);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (12, 3);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 4);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (2, 4);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (4, 4);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (7, 4);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 5);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 5);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (8, 5);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (13, 5);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 6);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 6);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (8, 6);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 7);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 7);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (9, 7);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 8);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (2, 8);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 8);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 9);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 9);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (8, 9);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (2, 10);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 10);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (8, 10);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 11);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (3, 11);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (9, 11);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 12);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (10, 12);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (11, 12);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 13);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (4, 13);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (6, 13);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (1, 14);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (4, 14);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (12, 14);

INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (15, 15);
INSERT INTO attribute_tupel (idattribute, idattribute_tupel_tree) VALUES (16, 15);



-- Dump attribute tupel children
INSERT INTO attribute_tupel_children (idattribute_tupel, idchild) VALUES (40, 41);


INSERT INTO geoposition (id, lat, lng) VALUES (1, 34, 120)
INSERT INTO geoposition (id, lat, lng) VALUES (2, 53, 90)
INSERT INTO geoposition (id, lat, lng) VALUES (3, 30, 90)
INSERT INTO geoposition (id, lat, lng) VALUES (4, 50, 94)
INSERT INTO geoposition (id, lat, lng) VALUES (5, 53, 90)
INSERT INTO geoposition (id, lat, lng) VALUES (6, 30, 104)
INSERT INTO geoposition (id, lat, lng) VALUES (7, 37, 94)
INSERT INTO geoposition (id, lat, lng) VALUES (8, 63, 100)
INSERT INTO geoposition (id, lat, lng) VALUES (9, 92, 100)
