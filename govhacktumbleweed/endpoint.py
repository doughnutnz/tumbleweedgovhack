
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

class RatingEndpoint(webapp2.RequestHandler):
    """Store a rating"""
    def post(self):
      installation_id = self.request.POST.get('installation_id')
      record_id = self.request.POST.get('record_id')
      playground_name = self.request.POST.get('playground_name')
      mark = self.request.POST.get('mark')
      rating = Rating.query(ndb.AND(Rating.installation_id == installation_id, 
                                    Rating.playground_name == playground_name)
                            ).fetch(1)
      if len(rating) > 0 :
        rating = rating[0]
        logging.debug(rating)
        rating.mark = int(mark)
        rating.record_id = int(record_id)
        rating.put()
      else:
        Rating(installation_id=installation_id, 
               record_id=int(record_id), 
               playground_name=playground_name,
               mark=int(mark)).put()
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
      
APPLICATION = webapp2.WSGIApplication([('/store_click', StoreClick),
                                       ('/rating', RatingEndpoint)], debug=True)