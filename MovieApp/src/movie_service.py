#!/usr/bin/env python
# encoding: utf-8
"""
movie_service.py: a RESTful movie data service

"""
import os
import codecs
import json
import re
import urlparse

import tornado.httpserver
import tornado.ioloop
import tornado.options
import tornado.web

from tornado.options import define, options

from rdflib import ConjunctiveGraph, URIRef, Literal, Namespace, RDF

define("port", default=8888, help="run on the given port", type=int)
define("movies", default="../data/movies.csv", help="movies data file")
define("actors", default="../data/actors.csv", help="actors data file")
define("mappings", default="../data/movie_actors.csv", help="key mapping file")

DCTERMS = Namespace('http://purl.org/dc/terms/')
DBPEDIA = Namespace('http://dbpedia.org/')
DBPEDIAPROP = Namespace('http://dbpedia.org/property/')
DBPEDIAOWL = Namespace('http://dbpedia.org/ontology/')

### Movie Web Service implementation ###

class MovieService(tornado.web.Application):
    """The Movie Service Web Application"""
    def __init__(self, db):
        handlers = [
            (r"/", HomeHandler),
            (r"/actors(\..+)?", ActorListHandler),
            (r"/actors/([0-9]+)(\..+)?", ActorHandler),
            (r"/movies(\..+)?", MovieListHandler),
            (r"/movies/([0-9]+)(\..+)?", MovieHandler),
            (r"/movies/([0-9]+)/actors(\..+)?", ActorsInMoviesHandler),
        	(r"/search", SearchHandler)

        ]
        settings = dict(
            template_path=os.path.join(os.path.dirname(__file__), "templates"),
            debug=True,
            autoescape=None,
        )
        tornado.web.Application.__init__(self, handlers, **settings)
        self.db = db
    
# Handlers
        
class BaseHandler(tornado.web.RequestHandler):
    """Functions common to all handlers"""
    @property
    def db(self):
        return self.application.db
        
    @property
    def base_uri(self):
        """Returns the Web service's base URI (e.g., http://localhost:8888)"""
        protocol = self.request.protocol
        host = self.request.headers.get('Host')
        return protocol + "://" + host
        
    def write_error(self, status_code, **kwargs):
        """Attach human-readable msg to error messages"""
        self.finish("Error %d - %s" % (status_code, kwargs['message']))
    

class HomeHandler(BaseHandler):
    def get(self):
        self.write("<html><body><h1>Movie Collection App</h1>by Jessica Kane</body></html>")

class ActorListHandler(BaseHandler):
    """Actor Collection Handler"""
    def get(self, format):
        actors = self.db.list_actors(self.base_uri)
        if format is None:
            self.redirect("/actors.json")
        elif format == ".xml":
            self.set_header("Content-Type", "application/xml")
            self.render("actor_list.xml", actors=actors)
        elif format == ".json":
            self.write(dict(actors=actors))
        else:
            self.write_error(401, message="Format %s not supported" % format)
            
class ActorHandler(BaseHandler):
    """Actor Individual Handler"""
    def get(self, actor_id, format):
		actor = self.db.list_actor_info(self.base_uri, actor_id)
		if actor == {}:
			self.write_error(404, message="Actor %s not found" % actor_id)
		if format is None:
			self.redirect("/actors/" + actor_id + ".json")
		elif format == ".xml":
			self.set_header("Content-Type", "application/xml")
			self.render("actor.xml", actor=actor)
		elif format == ".json":
			self.write(dict(actor=actor))
		elif format == ".ttl":
			self.set_header("Content-Type", "text/turtle")
			self.render("actor.ttl", actor=actor)
		elif format == ".rdf":
			self.set_header("Content-Type", "rdf/xml")
			self.render("actor.rdf", actor=actor)
		else:
			self.write_error(401, message="Format %s not supported" % format)
            
class MovieListHandler(BaseHandler):
    """Movie Collection Handler"""
    def get(self, format):
        movies = self.db.list_movies(self.base_uri)
        if format is None:
            self.redirect("/movies.json")
        elif format == ".xml":
            self.set_header("Content-Type", "application/xml")
            self.render("movie_list.xml", movies=movies)
        elif format == ".json":
            self.write(dict(movies=movies))
        else:
            self.write_error(401, message="Format %s not supported" % format)

	def post(self, format):	    
		title = self.get_arguement("Title", None)
		id = self.get_arguement("ID", None)
		syn = self.get_arguement("Synopsis", None)
		actors = self.get_arguement("Actors", None)

		valid = True

		if not id:
			self.write_error(401, message="No id provided")
			valid = False
		if not id.isdigit():
			self.write_error(401, message="Invalid id provided")
			valid = False

		actorsList = actors.split(",")
		for actor in actorsLit:
			if not id.isdigit():
				self.write_error(401, message="Invalid actor id provided")
				valid = False

		if valid:
			result = self.db.add_movie(id,title,syn,actorsList)
			if not result:
				self.write_error(401, message="Failed to add movie")
			else:
				self.set_header("Location", self.base_uri + "/movies/" + id)



class MovieHandler(BaseHandler):
    """Movie Individual Handler"""
    def get(self, movie_id, format):
		movie = self.db.list_movie_info(self.base_uri, movie_id)
		if movie == {}:
			self.write_error(404, message="Movie %s not found" % movie_id)
		if format is None:
			self.redirect("/movies/" + movie_id + ".json")
		elif format == ".xml":
			self.set_header("Content-Type", "application/xml")
			self.render("movie.xml", movie=movie)
		elif format == ".json":
			self.write(dict(movie=movie))
		elif format == ".ttl":
			self.set_header("Content-Type", "text/turtle")
			self.render("movie.ttl", movie=movie)
		elif format == ".rdf":
			self.set_header("Content-Type", "rdf/xml")
			self.render("movie.rdf", movie=movie)
		else:
			self.write_error(401, message="Format %s not supported" % format)

    def delete(self, movie_id, format):
		result = self.db.remove_movie(movie_id)
		if not result:
			self.write_error(401, message="Movie %s does not exist\n" % movie_id)

class ActorsInMoviesHandler(BaseHandler):
    """Collection of Actors In Movies Handler"""
    def get(self, movie_id, format):
        actors = self.db.list_actors(self.base_uri, movie_id)
        if format is None:
            self.redirect("/movies/" + movie_id + "/actors.json")
        elif format == ".xml":
            self.set_header("Content-Type", "application/xml")
            self.render("actor_list.xml", actors=actors)
        elif format == ".json":
            self.write(dict(actors=actors))
        else:
            self.write_error(401, message="Format %s not supported" % format)

class SearchHandler(BaseHandler):
    def get(self):
		term = self.get_argument("term", None)
		if (term):
			actors = self.db.list_actors(self.base_uri)
			hit_actors = []
			for actor in actors:
				if "name" in actor:
					if actor['name'].find(term) > -1:
						hit_actors.append(actor)
				elif "birthdate" in actor: 
					if actor['birthdate'].find(term) > -1:
						hit_actors.append(actor)
			
			movies = self.db.list_movies(self.base_uri)
			hit_movies = []
			for movie in movies:
				if "title" in movie:
					if movie['title'].find(term) > -1:
						hit_movies.append(movie)
				elif "synopsis" in movie:
					if movie['synopsis'].find(term) > -1:
						hit_movies.append(movie)

			print (len(hit_movies))

			searchDict = {}
			searchDict["actors"] = hit_actors	
			searchDict["movies"] = hit_movies		
			self.write(searchDict)
		else:
			self.write("<html><body>No search term entered.</body></html>")
		self.set_status(200)

### A dummy in-memory database implementation

class MovieDatabase(object):
    """A dummy in-memory database for handling movie data."""
    def __init__(self, movies_csv, actors_csv, mapping_csv):
        print "Loading data into memory...."
        mapping_data = self.read_from_csv(mapping_csv)
        movie_data = self.read_from_csv(movies_csv)
        actor_data = self.read_from_csv(actors_csv)
        self.movies = {}
        for movie in movie_data:
            self.movies[movie['id']] = movie
            actors = [actor['actor_id'] for actor in mapping_data
                            if actor['movie_id'] == movie['id']]
            self.movies[movie['id']]['actors'] = actors
        self.actors = {}
        for actor in actor_data:
            self.actors[actor['id']] = actor
            movies = [movie['movie_id'] for movie in mapping_data
                            if movie['actor_id'] == actor['id']]
            self.actors[actor['id']]['movies'] = movies
        
    # ACTOR CRUD operations
    
    def list_actors(self, base_uri, movie_id = None):
        """Returns a list of actors with IDs converted to URIs"""
        if movie_id is None:
            actors = self.actors.values()
        else:
            actors = [actor for actor in self.actors.values()
                            if movie_id in actor['movies']]
        actor_list = []
        for actor in actors:
            entry = {}
            entry['uri'] = base_uri + "/actors/" + actor['id']
            if actor.has_key('name'):
                entry['name'] = tornado.escape.xhtml_escape(actor['name'])
            if actor.has_key('birth_date'):
                entry['birthdate'] = tornado.escape.xhtml_escape(actor['birth_date'])
            if actor.has_key('movies'):
        		movie_urls = []
        		for movie in actor['movies']:
        			movie_urls.append(base_uri + "/movies/" + movie)
        		entry['movies'] = movie_urls
            actor_list.append(entry)
        return actor_list
        
    def list_actor_info(self, base_uri, actor_id):
        """Returns a single actor with ID converted to URIs"""
        actors = self.list_actors(base_uri)
        actor_entry = {}
        for actor in actors:
        	if (actor['uri'] == base_uri + "/actors/" + actor_id):
				actor_entry = actor
        return actor_entry

    # MOVIE CRUD operations

    def list_movies(self, base_uri, actor_id = None):
        """Returns a list of movies with IDs converted to URIs"""
        if actor_id is None:
            movies = self.movies.values()
        else:
            movies = [movie for movie in self.movies.values()
                            if actor_id in movie['actors']]
        movie_list = []
        for movie in movies:
            entry = {}
            entry['uri'] = base_uri + "/movies/" + movie['id']
            if movie.has_key('title'):
                entry['title'] = tornado.escape.xhtml_escape(movie['title'])
            if movie.has_key('synopsis'):
                entry['synopsis'] = tornado.escape.xhtml_escape(movie['synopsis'])
            if movie.has_key('actors'):
        		actor_urls = []
        		for actor in movie['actors']:
        			actor_urls.append(base_uri + "/actors/" + actor)
        		entry['actors'] = actor_urls
            movie_list.append(entry)
        return movie_list
        
    def list_movie_info(self, base_uri, movie_id):
        """Returns a single movie with IDs converted to URIs"""
        movies = self.list_movies(base_uri)
        movie_entry = {}
        for movie in movies:
        	if (movie['uri'] == base_uri + "/movies/" + movie_id):
				movie_entry = movie
        return movie_entry
        
    def add_movie(self, movie_id, title, syn, actorsList):
        """Adds a movie to the database"""
        if movie_id in self.movies:
    		return False
    	for actor_id in actorsList:
    		if not actor in self.actors:
    			return False
    	movie_info = {}
    	movie_info['title'] = title
    	movie_info['synopsis'] = syn
    	movie_info['actors'] = actorsList
		
    	movies[movie_id] = movie_info
        
    def remove_movie(self, movie_id):
        """Removes movie from the database"""
        if movie_id in self.movies:
    		del self.movies[movie_id]
    		return True
    	else:
    		return False
  
    # Data import
    
    def read_from_csv(self, csv_file):
        """Reads CSV entries into a list containing a set of dictionaries.
        CSV header row entries are taken as dictionary keys"""
        data = []
        with codecs.open(csv_file, 'r', encoding='utf-8') as csvfile:
            header = None
            for i, line in enumerate(csvfile):
                line_split = [x.strip() for x in line.split("|")]
                line_data = [x for x in line_split if len(x) > 0]
                if i == 0:
                    header = line_data
                else:
                    entry = {}
                    for i,datum in enumerate(line_data):
                        entry[header[i]] = datum
                    data.append(entry)
        print "Loaded %d entries from %s" % (len(data), csv_file)
        return data
           
### Script entry point ###

def main():
    tornado.options.parse_command_line()
    # Set up the database
    db = MovieDatabase(options.movies, options.actors, options.mappings)
    # Set up the Web application, pass the database
    movie_webservice = MovieService(db)
    # Set up HTTP server, pass Web application
    try:
        http_server = tornado.httpserver.HTTPServer(movie_webservice)
        http_server.listen(options.port)
        tornado.ioloop.IOLoop.instance().start()
    except KeyboardInterrupt:
        print "\nStopping service gracefully..."
    finally:
        tornado.ioloop.IOLoop.instance().stop()

if __name__ == "__main__":
    main()