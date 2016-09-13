
from google.appengine.ext import ndb
import webapp2
import logging
import json
import urllib2

class Rating(ndb.Model):
    """A model to store a playground rating"""
    record_id = ndb.IntegerProperty()
    playground_name = ndb.StringProperty()
    installation_id = ndb.StringProperty()
    mark = ndb.IntegerProperty()
    created_at = ndb.DateTimeProperty(auto_now_add=True)

class Playground(ndb.Model):
    """Model of a playground"""
    name = ndb.StringProperty()
    nb_clicks = ndb.IntegerProperty()
    nb_ratings = ndb.IntegerProperty()
    rating = ndb.FloatProperty()

class RatingEndpoint(webapp2.RequestHandler):
    """Store a rating"""
    def _update_playground_rating(self, playground_name, rating):
        playground = Playground.query(Playground.name == playground_name).fetch(1)

        if len(playground) > 0:
            playground = playground[0]
            logging.debug(playground)
            playground.rating = (playground.rating * playground.nb_ratings + rating) / (playground.nb_ratings + 1)
            playground.nb_ratings += 1
        else:
            playground = Playground(
                name = playground_name,
                nb_clicks = 1,
                nb_ratings = 1,
                rating = rating
                )

        playground.put()

    def _update_user_rating(self, installation_id, record_id, playground_name, mark):
        rating = Rating.query(
            ndb.AND(
                Rating.installation_id == installation_id, 
                Rating.playground_name == playground_name)
            ).fetch(1)

        # load existing record or create a new one
        if len(rating) > 0 :
            rating = rating[0]
            logging.debug(rating)
            rating.mark = int(mark)
            rating.record_id = int(record_id)
            rating.put()
        else:
            rating = Rating(
                installation_id=installation_id, 
                record_id=int(record_id), 
                playground_name=playground_name,
                mark=int(mark))

        # store record
        rating.put()        

    def post(self):
        installation_id = self.request.POST.get('installation_id')
        record_id = self.request.POST.get('record_id')
        playground_name = self.request.POST.get('playground_name')
        mark = self.request.POST.get('mark')

        # add or update user rating of the playground
        self._update_user_rating(installation_id, record_id, playground_name, mark)

        # add rating to the playground
        self._update_playground_rating(playground_name, int(mark))

        self.response.status = 200
    
    """Get a rating"""
    def get(self):
      playground_name = self.request.get('playground_name')
      installation_id = self.request.get('installation_id', None)
      if installation_id is None:
        rating_list = Rating.query(Rating.playground_name == playground_name)
      else:
        rating_list = Rating.query(ndb.AND(Rating.playground_name == playground_name,
                                           Rating.installation_id == installation_id)
                                  )                            
      mark_list = [r.mark for r in rating_list]
      if len(mark_list) > 0:
        body = {'rating': round(sum(mark_list)/len(mark_list), 1), 'count': len(mark_list)}
      else:
        body = {'rating': None, 'count': 0}
      self.response.status = 200
      self.response.headers['Content-Type'] = 'application/json'   
      self.response.out.write(json.dumps(body))
      
class Click(ndb.Model):
    """A model to store clicks"""
    installation_id = ndb.StringProperty()
    record_id = ndb.IntegerProperty()
    playground_name = ndb.StringProperty()
    latitude = ndb.FloatProperty()
    longitude = ndb.FloatProperty()
    created_at = ndb.DateTimeProperty(auto_now_add=True)
                                           
class StoreClick(webapp2.RequestHandler):
    """Store a click"""
    def post(self):
      installation_id = self.request.POST.get('installation_id')
      record_id = self.request.POST.get('record_id')
      playground_name = self.request.POST.get('playground_name')
      latitude = self.request.POST.get('latitude')
      longitude = self.request.POST.get('longitude')
      logging.debug(self.request.POST)
      logging.debug(installation_id)
      logging.debug(record_id)
      logging.debug(playground_name)
      Click(installation_id=installation_id, 
            record_id=int(record_id), 
            playground_name=playground_name,
            latitude=float(latitude),
            longitude=float(longitude)).put()
      self.response.status = 200

class Favorites(ndb.Model):
    """A model to store the list of favorite playgrounds per installation_id"""
    installation_id = ndb.StringProperty()
    record_id_list = ndb.IntegerProperty(repeated=True)
    playground_name_list = ndb.StringProperty(repeated=True)
                                           
class RegisterFavorite(webapp2.RequestHandler):
    """register a playground as a favorite"""
    def post(self):
      installation_id = self.request.POST.get('installation_id')
      record_id = self.request.POST.get('record_id')
      playground_name = self.request.POST.get('playground_name')
      logging.debug(self.request.POST)
      logging.debug(installation_id)
      logging.debug(record_id)
      logging.debug(playground_name)
      favorites = Favorites.query(Favorites.installation_id == installation_id).fetch(1)
      if len(favorites) > 0 :
        favorites.record_id_list.append(int(record_id))
        favorites.playground_name_list.append(playground_name)
        favorites.put()
      else:
        Favorites(installation_id=installation_id, 
                  record_id_list=[int(record_id)], 
                  playground_name_list=[playground_name]).put()
      self.response.status = 200   


APPLICATION = webapp2.WSGIApplication([('/store_click', StoreClick),
                                       ('/rating', RatingEndpoint),
                                       ('/register_favorite', RegisterFavorite)], debug=True)