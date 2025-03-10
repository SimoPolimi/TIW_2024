CREATE TABLE `comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_image` int NOT NULL,
  `id_user` int NOT NULL,
  `date` datetime NOT NULL,
  `text` varchar(400) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_image_idx` (`id_image`),
  KEY `id_user_idx` (`id_user`) ,
  CONSTRAINT `fk_image` FOREIGN KEY (`id_image`) REFERENCES `image` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`)
)

CREATE TABLE `album` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(45) NOT NULL,
  `owner` int NOT NULL,
  `creation_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_album_idx` (`owner`),
  CONSTRAINT `fk_user_album` FOREIGN KEY (`owner`) REFERENCES `user` (`id`)
);

CREATE TABLE `album_image` (
  `id_album` int NOT NULL,
  `id_image` int NOT NULL,
  PRIMARY KEY (`id_album`,`id_image`),
  KEY `fk_image_album_idx` (`id_image`),
  CONSTRAINT `fk_album_image` FOREIGN KEY (`id_album`) REFERENCES `album` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_image_album` FOREIGN KEY (`id_image`) REFERENCES `image` (`id`) ON DELETE CASCADE
);

CREATE TABLE `image` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_user` int NOT NULL,
  `title` varchar(45) NOT NULL,
  `creation_date` datetime NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `path` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_idx` (`id_user`),
  CONSTRAINT `fk_user_image` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`)
);

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `mail` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `mail_UNIQUE` (`mail`)
);

CREATE TABLE `user_image_order` (
  `id_user` int NOT NULL,
  `id_album` int NOT NULL,
  `id_image` int NOT NULL,
  `position` int NOT NULL,
  PRIMARY KEY (`id_user`,`id_album`,`id_image`),
  KEY `fk_order_album_idx` (`id_album`,`id_image`),
  CONSTRAINT `fk_order_album_image` FOREIGN KEY (`id_album`, `id_image`) REFERENCES `album_image` (`id_album`, `id_image`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
);