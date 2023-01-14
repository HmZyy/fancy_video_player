class Subtitle {
  String url;
  String label;
  Subtitle(this.url, this.label);

  Map<String, dynamic> toJson() {
    return {'url': url, 'label': label};
  }

  factory Subtitle.fromJson(Map<String, dynamic> json) {
    return Subtitle(json['url'], json['label']);
  }
}
